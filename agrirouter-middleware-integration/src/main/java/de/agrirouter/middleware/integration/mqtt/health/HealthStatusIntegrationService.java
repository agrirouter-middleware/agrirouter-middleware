package de.agrirouter.middleware.integration.mqtt.health;

import com.dke.data.agrirouter.api.service.parameters.PingParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.PingServiceImpl;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service to check the health status of an endpoint.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthStatusIntegrationService {

    private final MqttClientManagementService mqttClientManagementService;
    private final HealthStatusMessages healthStatusMessages;
    private final LastKnownHealthyMessages lastKnownHealthyMessages;

    /**
     * Publish a health status message to the internal topic.
     *
     * @param endpoint The endpoint.
     */
    public void publishHealthStatusMessage(Endpoint endpoint) {
        var mqttClient = mqttClientManagementService.get(endpoint);
        var messageId = new AtomicReference<>("not_set_yet");
        mqttClient.ifPresentOrElse(client -> {
                    if (client.isConnected()) {
                        var pingService = new PingServiceImpl(client);
                        var onboardingResponse = endpoint.asOnboardingResponse();
                        var parameters = new PingParameters();
                        parameters.setOnboardingResponse(onboardingResponse);
                        log.info("Publishing the health check message to the topic, but not saving it as a message waiting for ACK to reduce overload.");
                        messageId.set(pingService.send(parameters));
                    } else {
                        log.error("Could not publish the health check message. MQTT client is not connected.");
                        throw new BusinessException(ErrorMessageFactory.couldNotPublishHealthMessageSinceClientIsNotConnected());
                    }
                }, () -> log.warn("Could not find or create a MQTT client for endpoint with the external endpoint ID '{}'.", endpoint.getExternalEndpointId())
        );
        healthStatusMessages.put(HealthStatusMessage.builder().
                agrirouterEndpointId(endpoint.getAgrirouterEndpointId())
                .healthStatus(null)
                .hasBeenReturned(false)
                .messageId(messageId.get())
                .build());
    }

    /**
     * Marks a health status message as received for the given agrirouter endpoint ID.
     *
     * @param agrirouterEndpointId The ID of the agrirouter endpoint for which the health status message is to be marked as received.
     */
    public void markHealthMessageAsReceived(String agrirouterEndpointId) {
        healthStatusMessages.remove(agrirouterEndpointId);
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

    /**
     * Set the last known healthy status for the given agrirouter endpoint ID.
     *
     * @param agrirouterEndpointId The endpoint ID.
     */
    public void setLastKnownHealthyStatus(String agrirouterEndpointId) {
        lastKnownHealthyMessages.put(agrirouterEndpointId);
    }

    /**
     * Check if there is a pending health status response for the given endpoint ID.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @return True if there is a pending health status response, false otherwise.
     */
    public boolean hasPendingResponse(String agrirouterEndpointId) {
        return healthStatusMessages.get(agrirouterEndpointId).isPresent();
    }

    /**
     * Get the health status message for the given endpoint ID.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @return The health status message.
     */
    public Optional<HealthStatusMessage> getHealthStatusMessage(String agrirouterEndpointId) {
        var healthStatusMessage = healthStatusMessages.get(agrirouterEndpointId);
        if (healthStatusMessage.isPresent() && healthStatusMessage.get().isHasBeenReturned()) {
            return healthStatusMessage;
        }
        return Optional.empty();
    }

    /**
     * Removes all pending health status messages for a given Agrirouter endpoint.
     *
     * @param agrirouterEndpointId The ID of the Agrirouter endpoint whose pending health status messages are to be removed.
     */
    public void removeAllPendingHealthStatusMessagesForEndpoint(String agrirouterEndpointId) {
        healthStatusMessages.remove(agrirouterEndpointId);
    }
}
