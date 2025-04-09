package de.agrirouter.middleware.integration.mqtt;

import agrirouter.request.payload.endpoint.Capabilities;
import agrirouter.response.payload.account.Endpoints;
import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.events.*;
import de.agrirouter.middleware.integration.mqtt.list_endpoints.ListEndpointsMessages;
import de.agrirouter.middleware.integration.mqtt.list_endpoints.MessageRecipient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.charset.StandardCharsets;
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
    private final ListEndpointsMessages listEndpointsMessages;

    @Setter
    @Getter
    private String clientIdOfTheRouterDevice;

    public MessageHandlingCallback(ApplicationEventPublisher applicationEventPublisher,
                                   DecodeMessageService decodeMessageService,
                                   MqttStatistics mqttStatistics,
                                   ListEndpointsMessages listEndpointsMessages) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.decodeMessageService = decodeMessageService;
        this.mqttStatistics = mqttStatistics;
        this.listEndpointsMessages = listEndpointsMessages;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        log.error("Connection to MQTT broker lost.", throwable);
        mqttStatistics.increaseNumberOfConnectionLosses();
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        try {
            log.debug("Message '{}' with QoS {} arrived.", mqttMessage.getId(), mqttMessage.getQos());
            log.trace("Message payload for message '{}' >>> {}", mqttMessage.getId(), StringUtils.toEncodedString(mqttMessage.getPayload(), StandardCharsets.UTF_8));
            mqttStatistics.increaseNumberOfMessagesArrived();
            var payload = StringUtils.toEncodedString(mqttMessage.getPayload(), StandardCharsets.UTF_8);
            handleAgrirouterMessage(payload);
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
        applicationEventPublisher.publishEvent(new ClearSubscriptionsForMqttClientEvent(this, clientIdOfTheRouterDevice));
    }
}
