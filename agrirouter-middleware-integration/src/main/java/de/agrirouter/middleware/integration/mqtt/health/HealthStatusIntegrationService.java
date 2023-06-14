package de.agrirouter.middleware.integration.mqtt.health;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Service to check the health status of an endpoint.
 */
@Slf4j
@Service
public class HealthStatusIntegrationService {

    private final MqttClientManagementService mqttClientManagementService;
    private final HealthStatusMessages healthStatusMessages;

    public HealthStatusIntegrationService(MqttClientManagementService mqttClientManagementService,
                                          HealthStatusMessages healthStatusMessages) {
        this.mqttClientManagementService = mqttClientManagementService;
        this.healthStatusMessages = healthStatusMessages;
    }

    /**
     * Publish a health status message to the internal topic.
     *
     * @param endpoint The endpoint.
     */
    public void publishHealthStatusMessage(Endpoint endpoint) {
        Optional<IMqttClient> mqttClient = mqttClientManagementService.get(endpoint);
        mqttClient.ifPresentOrElse(client -> {
            if (client.isConnected()) {
                try {
                    var onboardingResponse = endpoint.asOnboardingResponse();
                    var healthMessage = new MqttMessage();
                    var healthStatusMessage = new HealthStatusMessage();
                    healthStatusMessage.setAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
                    healthMessage.setPayload(healthStatusMessage.asJson().getBytes());
                    healthMessage.setQos(0);
                    client.publish(onboardingResponse.getConnectionCriteria().getCommands(), healthMessage);
                    healthStatusMessages.put(healthStatusMessage);
                } catch (MqttException e) {
                    log.error("Could not publish the health check message.", e);
                    throw new BusinessException(ErrorMessageFactory.couldNotPublishHealthMessage());
                }
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
     * @return True if the endpoint is healthy, false otherwise.
     */
    public boolean isHealthy(String agrirouterEndpointId) {
        var healthStatusMessage = healthStatusMessages.get(agrirouterEndpointId);
        if (healthStatusMessage == null) {
            log.warn("No health status message found for endpoint ID {}.", agrirouterEndpointId);
            return false;
        }
        var hasBeenReturned = healthStatusMessage.isHasBeenReturned();
        if (!hasBeenReturned) {
            log.debug("Health status message for endpoint ID {} has not been returned.", agrirouterEndpointId);
        } else {
            log.info("Health status message for endpoint ID {} has been returned.", agrirouterEndpointId);
            healthStatusMessages.remove(agrirouterEndpointId);
        }
        return hasBeenReturned;
    }

    /**
     * Check if there is a pending health status response for the given endpoint ID.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @return True if there is a pending health status response, false otherwise.
     */
    public boolean hasPendingResponse(String agrirouterEndpointId) {
        var healthStatusMessage = healthStatusMessages.get(agrirouterEndpointId);
        return healthStatusMessage != null;
    }
}
