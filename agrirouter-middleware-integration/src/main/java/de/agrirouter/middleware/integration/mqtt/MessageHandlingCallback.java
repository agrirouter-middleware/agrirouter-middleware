package de.agrirouter.middleware.integration.mqtt;

import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.google.gson.Gson;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.events.*;
import de.agrirouter.middleware.integration.mqtt.health.HealthStatusMessage;
import de.agrirouter.middleware.integration.mqtt.health.HealthStatusMessages;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.charset.StandardCharsets;

/**
 * Callback for all MQTT connections to the agrirouter.
 */
@Slf4j
public class MessageHandlingCallback implements MqttCallback {

    private static final Gson GSON = new Gson();

    private final ApplicationEventPublisher applicationEventPublisher;
    private final DecodeMessageService decodeMessageService;
    private final MqttStatistics mqttStatistics;
    private final HealthStatusMessages healthStatusMessages;

    public MessageHandlingCallback(ApplicationEventPublisher applicationEventPublisher,
                                   DecodeMessageService decodeMessageService,
                                   MqttStatistics mqttStatistics,
                                   HealthStatusMessages healthStatusMessages) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.decodeMessageService = decodeMessageService;
        this.mqttStatistics = mqttStatistics;
        this.healthStatusMessages = healthStatusMessages;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        log.warn("Connection lost. There is at least one endpoint unreachable until the next connection check.");
        mqttStatistics.increaseNumberOfConnectionLosses();
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
            case ACK_FOR_FEED_HEADER_LIST -> {
                log.trace("This was a query result for a message header query that is used.");
                mqttStatistics.increaseNumberOfAcknowledgements();
                applicationEventPublisher.publishEvent(new EndpointStatusUpdateEvent(this, fetchMessageResponse.getSensorAlternateId(), fetchMessageResponse));
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
                applicationEventPublisher.publishEvent(new UpdateRecipientsForEndpointEvent(this, fetchMessageResponse.getSensorAlternateId(), fetchMessageResponse));
                applicationEventPublisher.publishEvent(new MessageAcknowledgementEvent(this, decodedMessageResponse));
            }
            default -> {
                log.trace("This was a unknown message.");
                mqttStatistics.increaseNumberOfUnknownMessages();
                applicationEventPublisher.publishEvent(new UnknownMessageEvent(this, fetchMessageResponse));
                applicationEventPublisher.publishEvent(new MessageAcknowledgementEvent(this, decodedMessageResponse));
            }
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
        try {
            if (null != iMqttDeliveryToken.getMessage()) {
                log.trace("Message payload for message '{}' >>> {}", iMqttDeliveryToken.getMessageId(), StringUtils.toEncodedString(iMqttDeliveryToken.getMessage().getPayload(), StandardCharsets.UTF_8));
            }
        } catch (MqttException e) {
            log.error("Could not log message content.", e);
        }
    }

}
