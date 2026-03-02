package de.agrirouter.middleware.integration.mqtt;

import agrirouter.request.payload.endpoint.Capabilities;
import agrirouter.response.payload.account.Endpoints;
import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.events.*;
import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import de.agrirouter.middleware.integration.mqtt.health.HealthStatusMessages;
import de.agrirouter.middleware.integration.mqtt.list_endpoints.ListEndpointsMessages;
import de.agrirouter.middleware.integration.mqtt.list_endpoints.MessageRecipient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;

/**
 * Callback for all MQTT connections to the agrirouter.
 */
@Slf4j
@RequiredArgsConstructor
public class MessageHandlingCallback {

    private static final Gson GSON = new Gson();
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DecodeMessageService decodeMessageService;
    private final MqttStatistics mqttStatistics;
    private final ListEndpointsMessages listEndpointsMessages;
    private final HealthStatusMessages healthStatusMessages;

    @Setter
    @Getter
    private String clientIdOfTheRouterDevice;

    public void connectionLost(Throwable throwable) {
        log.error("Connection to MQTT broker lost.", throwable);
        mqttStatistics.increaseNumberOfConnectionLosses();
    }

    /**
     * Handles an incoming MQTT message published on a subscribed topic.
     * Decodes the agrirouter message payload and dispatches the appropriate application event
     * based on the message type (acknowledgement, push notification, cloud registration, etc.).
     *
     * @param publish The incoming MQTT publish message containing the topic and payload.
     */
    public void handleMessage(Mqtt3Publish publish) {
        try {
            var payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
            log.debug("Message arrived on topic '{}'.", publish.getTopic());
            log.trace("Message payload >>> {}", payload);
            mqttStatistics.increaseNumberOfMessagesArrived();
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
                handleHealthStatusMessage(decodedMessageResponse);
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

    private void handleHealthStatusMessage(DecodeMessageResponse decodedMessageResponse) {
        healthStatusMessages.getByMessageId(decodedMessageResponse.getResponseEnvelope().getApplicationMessageId()).ifPresent(healthStatusMessage -> {
            log.debug("Received health status message for endpoint ID {}.", decodedMessageResponse.getResponseEnvelope().getApplicationMessageId());
            healthStatusMessage.setHealthStatus(decodedMessageResponse.getResponseEnvelope().getType());
            healthStatusMessage.setHasBeenReturned(true);
            healthStatusMessages.put(healthStatusMessage);
        });
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
                    if (messageType.getDirection().name().equalsIgnoreCase(Capabilities.CapabilitySpecification.Direction.RECEIVE.name())) {
                        final var messageRecipient = createMessageRecipient(e, messageType, now);
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

    private static MessageRecipient createMessageRecipient(Endpoints.ListEndpointsResponse.Endpoint e, Endpoints.ListEndpointsResponse.MessageType messageType, Instant now) {
        final var messageRecipient = new MessageRecipient();
        messageRecipient.setAgrirouterEndpointId(e.getEndpointId());
        messageRecipient.setEndpointName(e.getEndpointName());
        messageRecipient.setEndpointType(e.getEndpointType());
        messageRecipient.setExternalId(e.getExternalId());
        messageRecipient.setTechnicalMessageType(TemporaryContentMessageType.fromKey(messageType.getTechnicalMessageType()));
        messageRecipient.setTimestamp(now);
        return messageRecipient;
    }

    public void connectComplete(String serverURI) {
        applicationEventPublisher.publishEvent(new ClearSubscriptionsForMqttClientEvent(this, clientIdOfTheRouterDevice));
    }
}
