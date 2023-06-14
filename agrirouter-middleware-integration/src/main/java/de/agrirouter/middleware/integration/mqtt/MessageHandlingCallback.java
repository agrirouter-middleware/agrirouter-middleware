package de.agrirouter.middleware.integration.mqtt;

import agrirouter.request.payload.endpoint.Capabilities;
import agrirouter.response.payload.account.Endpoints;
import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.events.*;
import de.agrirouter.middleware.integration.mqtt.health.HealthStatusMessage;
import de.agrirouter.middleware.integration.mqtt.health.HealthStatusMessages;
import de.agrirouter.middleware.integration.mqtt.list_endpoints.ListEndpointsMessages;
import de.agrirouter.middleware.integration.mqtt.list_endpoints.MessageRecipient;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

/**
 * Callback for all MQTT connections to the agrirouter.
 */
@Slf4j
public class MessageHandlingCallback implements MqttCallbackExtended {

    private static final Gson GSON = new Gson();
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DecodeMessageService decodeMessageService;
    private final MqttStatistics mqttStatistics;
    private final HealthStatusMessages healthStatusMessages;
    private final ListEndpointsMessages listEndpointsMessages;
    private final SubscriptionsForMqttClient subscriptionsForMqttClient;
    private final MqttClientManagementService mqttClientManagementService;
    private final Bucket bucket;
    private boolean currentlyDisconnecting = false;

    @Setter
    @Getter
    private IMqttClient mqttClient;

    @Setter
    @Getter
    private String clientIdOfTheRouterDevice;


    public MessageHandlingCallback(ApplicationEventPublisher applicationEventPublisher,
                                   DecodeMessageService decodeMessageService,
                                   MqttStatistics mqttStatistics,
                                   HealthStatusMessages healthStatusMessages,
                                   ListEndpointsMessages listEndpointsMessages,
                                   SubscriptionsForMqttClient subscriptionsForMqttClient,
                                   MqttClientManagementService mqttClientManagementService) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.decodeMessageService = decodeMessageService;
        this.mqttStatistics = mqttStatistics;
        this.healthStatusMessages = healthStatusMessages;
        this.listEndpointsMessages = listEndpointsMessages;
        this.subscriptionsForMqttClient = subscriptionsForMqttClient;
        this.mqttClientManagementService = mqttClientManagementService;

        var limit = Bandwidth.classic(10, Refill.intervally(60, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder().addLimit(limit).build();
    }

    @Override
    public void connectionLost(Throwable throwable) {
        if (!currentlyDisconnecting) {
            if (bucket.tryConsume(1)) {
                log.info("Connection lost for client {}, but rate limit was not exceeded. There are {} token left before the limit will be hit.", this.mqttClient.getClientId(), bucket.getAvailableTokens());
                mqttStatistics.increaseNumberOfConnectionLosses();
            } else {
                log.error("Connection lost for client {} and the rate limit exceeded. Forcefully disconnecting the client and removing it from the cache.", this.mqttClient.getClientId());
                currentlyDisconnecting = true;
                mqttClientManagementService.kill(clientIdOfTheRouterDevice);
            }
        }
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        try {
            log.debug("Message '{}' with QoS {} arrived.", mqttMessage.getId(), mqttMessage.getQos());
            log.trace("Message payload for message '{}' >>> {}", mqttMessage.getId(), StringUtils.toEncodedString(mqttMessage.getPayload(), StandardCharsets.UTF_8));
            mqttStatistics.increaseNumberOfMessagesArrived();
            var payload = StringUtils.toEncodedString(mqttMessage.getPayload(), StandardCharsets.UTF_8);
            if (StringUtils.isNotBlank(payload)) {
                if (StringUtils.startsWith(payload, HealthStatusMessage.MESSAGE_PREFIX)) {
                    handleHealthStatusMessage(payload);
                } else {
                    handleAgrirouterMessage(payload);
                }
            }
        } catch (BusinessException e) {
            log.error("An internal business exception occurred.", e);
        } catch (Exception e) {
            log.error("An unknown error occurred.", e);
        }
    }

    private void handleAgrirouterMessage(String payload) {
        final var fetchMessageResponse = GSON.fromJson(payload, FetchMessageResponse.class);
        final var decodedMessageResponse = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());
        switch (decodedMessageResponse.getResponseEnvelope().getType()) {
            case ACK, ACK_WITH_MESSAGES, ACK_WITH_FAILURE -> {
                log.trace("This was a message acknowledgement.");
                mqttStatistics.increaseNumberOfAcknowledgements();
                applicationEventPublisher.publishEvent(new MessageAcknowledgementEvent(this, decodedMessageResponse));
            }
            case PUSH_NOTIFICATION -> {
                log.trace("This was a push notification.");
                mqttStatistics.increaseNumberOfPushNotifications();
                applicationEventPublisher.publishEvent(new PushMessageEvent(this, fetchMessageResponse));
            }
            case ACK_FOR_FEED_MESSAGE -> {
                log.trace("This was a query result for a message query.");
                mqttStatistics.increaseNumberOfAcknowledgements();
                applicationEventPublisher.publishEvent(new MessageQueryResultEvent(this, fetchMessageResponse));
                applicationEventPublisher.publishEvent(new MessageAcknowledgementEvent(this, decodedMessageResponse));
            }
            case CLOUD_REGISTRATIONS -> {
                log.trace("This was a cloud registration.");
                mqttStatistics.increaseNumberOfCloudRegistrations();
                applicationEventPublisher.publishEvent(new CloudRegistrationEvent(this, decodedMessageResponse.getResponseEnvelope().getApplicationMessageId(), fetchMessageResponse));
                applicationEventPublisher.publishEvent(new MessageAcknowledgementEvent(this, decodedMessageResponse));
            }
            case ENDPOINTS_LISTING -> {
                log.trace("This was an endpoint listing.");
                mqttStatistics.increaseNumberOfEndpointListings();
                handleListEndpointsMessage(fetchMessageResponse);
                applicationEventPublisher.publishEvent(new MessageAcknowledgementEvent(this, decodedMessageResponse));
            }
            default -> {
                log.trace("This was a unknown / unsupported message.");
                mqttStatistics.increaseNumberOfUnknownMessages();
                applicationEventPublisher.publishEvent(new UnknownMessageEvent(this, fetchMessageResponse));
                applicationEventPublisher.publishEvent(new MessageAcknowledgementEvent(this, decodedMessageResponse));
            }
        }
    }

    private void handleListEndpointsMessage(FetchMessageResponse fetchMessageResponse) {
        try {
            var existingListEndpointsMessage = listEndpointsMessages.get(fetchMessageResponse.getSensorAlternateId());
            if (null != existingListEndpointsMessage) {
                final var decodedMessageResponse = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());
                final Endpoints.ListEndpointsResponse listEndpointsResponse;
                listEndpointsResponse = Endpoints.ListEndpointsResponse.parseFrom(decodedMessageResponse.getResponsePayloadWrapper().getDetails().getValue());
                var messageRecipients = new ArrayList<MessageRecipient>();
                final var now = Instant.now();
                listEndpointsResponse.getEndpointsList().forEach(e -> e.getMessageTypesList().forEach(messageType -> {
                    if (messageType.getDirection().name().equalsIgnoreCase(Capabilities.CapabilitySpecification.Direction.SEND.name())) {
                        final var messageRecipient = new MessageRecipient();
                        messageRecipient.setAgrirouterEndpointId(e.getEndpointId());
                        messageRecipient.setEndpointName(e.getEndpointName());
                        messageRecipient.setEndpointType(e.getEndpointType());
                        messageRecipient.setExternalId(e.getExternalId());
                        messageRecipient.setTechnicalMessageType(messageType.getTechnicalMessageType());
                        messageRecipient.setDirection(messageType.getDirection().name());
                        messageRecipient.setTimestamp(now);
                        log.trace("Added recipient: {}", messageRecipient);
                        messageRecipients.add(messageRecipient);
                    } else {
                        log.debug("Ignoring message type with direction {} for endpoint '{}'.", messageType.getDirection().name(), e.getEndpointId());
                    }
                }));
                log.debug("There were {} recipients found for the endpoint '{}'.", messageRecipients.size(), fetchMessageResponse.getSensorAlternateId());
                log.trace("{}", messageRecipients);
                existingListEndpointsMessage.setMessageRecipients(messageRecipients);
                existingListEndpointsMessage.setHasBeenReturned(true);
                listEndpointsMessages.put(existingListEndpointsMessage);
            } else {
                log.warn("Received list endpoints message for unknown former message for endpoint: {}", fetchMessageResponse.getSensorAlternateId());
            }
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse list endpoints response.", e);
        }
    }

    private void handleHealthStatusMessage(String payload) {
        log.info("Received health status message: {}", payload);
        var strippedPayload = StringUtils.removeStart(payload, HealthStatusMessage.MESSAGE_PREFIX).trim();
        var healthStatusMessage = GSON.fromJson(strippedPayload, HealthStatusMessage.class);
        var existingHealthStatusMessage = healthStatusMessages.get(healthStatusMessage.getAgrirouterEndpointId());
        if (null != existingHealthStatusMessage) {
            existingHealthStatusMessage.setHasBeenReturned(true);
            healthStatusMessages.put(existingHealthStatusMessage);
        } else {
            log.warn("Received health status message for unknown former message for endpoint: {}", healthStatusMessage.getAgrirouterEndpointId());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        log.debug("Delivery for message '{}' complete.", iMqttDeliveryToken.getMessageId());
        mqttStatistics.increaseNumberOfMessagesPublished();
        try {
            if (null != iMqttDeliveryToken.getMessage()) {
                log.trace("Message payload for message '{}' >>> {}", iMqttDeliveryToken.getMessageId(), StringUtils.toEncodedString(iMqttDeliveryToken.getMessage().getPayload(), StandardCharsets.UTF_8));
                mqttStatistics.increasePayloadReceived(iMqttDeliveryToken.getMessage().getPayload().length);
            }
        } catch (MqttException e) {
            log.error("Could not log message content.", e);
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (!currentlyDisconnecting) {
            if (reconnect) {
                mqttStatistics.increaseNumberOfReconnects();
                log.debug("Reconnected client {} to MQTT broker at {}.", mqttClient.getClientId(), serverURI);
                var allFormerTopics = subscriptionsForMqttClient.getAll(mqttClient.getClientId());
                subscriptionsForMqttClient.clear(mqttClient.getClientId());
                allFormerTopics.forEach(topic -> {
                    try {
                        var iMqttToken = mqttClient.subscribeWithResponse(topic);
                        if (iMqttToken.isComplete()) {
                            subscriptionsForMqttClient.add(mqttClient.getClientId(), topic);
                        } else {
                            log.warn("Could not subscribe to topic '{}'.", topic);
                        }
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }

                });
            } else {
                mqttStatistics.increaseNumberOfConnects();
                log.info("Since this was a first connect and the subscription is done in the subscribe method, we do not need to do anything here.");
                log.debug("Connected client {} to MQTT broker at {}.", mqttClient.getClientId(), serverURI);
            }
        }
    }
}
