package de.agrirouter.middleware.integration.mqtt.health;

import com.dke.data.agrirouter.api.enums.SystemMessageType;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.mqtt.health.internal.PingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Service to check the health status of an endpoint.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthStatusIntegrationService {

    private final MqttClientManagementService mqttClientManagementService;
    private final HealthStatusMessages healthStatusMessages;
    private final PingService pingService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final LastKnownHealthyMessages lastKnownHealthyMessages;

    /**
     * Publish a health status message to the internal topic.
     *
     * @param endpoint The endpoint.
     */
    public void publishHealthStatusMessage(Endpoint endpoint) {
        Optional<IMqttClient> mqttClient = mqttClientManagementService.get(endpoint);
        mqttClient.ifPresentOrElse(client -> {
            if (client.isConnected()) {
                var onboardingResponse = endpoint.asOnboardingResponse();
                var messageId = pingService.send(client, onboardingResponse);
                var healthStatusMessage = HealthStatusMessageWaitingForAck.builder()
                        .agrirouterEndpointId(endpoint.getAgrirouterEndpointId())
                        .messageId(messageId)
                        .healthStatus(HealthStatus.PENDING)
                        .build();
                healthStatusMessages.put(healthStatusMessage);
                log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
                MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
                messageWaitingForAcknowledgement.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
                messageWaitingForAcknowledgement.setMessageId(messageId);
                messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_PING.getKey());
                messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
            } else {
                log.error("Could not publish the health check message. MQTT client is not connected.");
                throw new BusinessException(ErrorMessageFactory.couldNotPublishHealthMessageSinceClientIsNotConnected());
            }
        }, () -> log.warn("Could not find or create a MQTT client for endpoint with the external endpoint ID '{}'.", endpoint.getExternalEndpointId()));
    }

    /**
     * Check if the endpoint is healthy. In this case would mean that the last health status message was received from the agrirouter.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @return The health status.
     */
    public HealthStatus determineHealthStatus(String agrirouterEndpointId) {
        var healthStatusMessage = healthStatusMessages.get(agrirouterEndpointId);
        if (healthStatusMessage == null) {
            log.warn("No health status message found for endpoint ID {}.", agrirouterEndpointId);
        } else {

            if (healthStatusMessage.getHealthStatus().equals(HealthStatus.PENDING)) {
                log.debug("Health status message for endpoint ID {} is still pending.", agrirouterEndpointId);
                return HealthStatus.PENDING;
            }

            if (healthStatusMessage.getHealthStatus().equals(HealthStatus.HEALTHY)) {
                log.debug("Health status message for endpoint ID {} is healthy.", agrirouterEndpointId);
                healthStatusMessages.remove(agrirouterEndpointId);
                return HealthStatus.HEALTHY;
            }

            if (healthStatusMessage.getHealthStatus().equals(HealthStatus.UNHEALTHY)) {
                log.debug("Health status message for endpoint ID {} is no longer available.", agrirouterEndpointId);
                return HealthStatus.UNHEALTHY;
            }
        }
        return HealthStatus.UNKNOWN;
    }

    /**
     * Marks a health status message as received for the given agrirouter endpoint ID.
     *
     * @param agrirouterEndpointId The ID of the agrirouter endpoint for which the health status message is to be marked as received.
     */
    public void markHealthMessageAsReceived(String agrirouterEndpointId) {
        var healthStatusMessage = healthStatusMessages.get(agrirouterEndpointId);
        if (healthStatusMessage != null) {
            healthStatusMessage.setHealthStatus(HealthStatus.HEALTHY);
            healthStatusMessages.put(healthStatusMessage);
            lastKnownHealthyMessages.put(agrirouterEndpointId);
        }
    }

    /**
     * Marks a health status message as lost for the given agrirouter endpoint ID.
     *
     * @param agrirouterEndpointId The ID of the agrirouter endpoint for which the health status message is to be marked as lost.
     */
    public void markHealthMessageAsLost(String agrirouterEndpointId) {
        var healthStatusMessage = healthStatusMessages.get(agrirouterEndpointId);
        if (healthStatusMessage != null) {
            healthStatusMessage.setHealthStatus(HealthStatus.UNHEALTHY);
            healthStatusMessages.put(healthStatusMessage);
        }
    }

    /**
     * Get the last known healthy status for the given agrirouter endpoint ID.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @return The last known healthy status.
     */
    public Optional<Instant> getLastKnownHealthyStatus(String agrirouterEndpointId) {
        return lastKnownHealthyMessages.get(agrirouterEndpointId);
    }
}
