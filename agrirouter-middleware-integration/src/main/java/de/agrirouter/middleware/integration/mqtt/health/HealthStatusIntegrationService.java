package de.agrirouter.middleware.integration.mqtt.health;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorKey;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessage;
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
     * @param onboardingResponse The onboarding response.
     */
    public void publishHealthStatusMessage(OnboardingResponse onboardingResponse) {
        Optional<IMqttClient> mqttClient = mqttClientManagementService.get(onboardingResponse);
        mqttClient.ifPresentOrElse(client -> {
            if (client.isConnected()) {
                try {
                    var healthMessage = new MqttMessage();
                    var healthStatusMessage = new HealthStatusMessage();
                    healthStatusMessage.setTimestamp(Instant.now().toEpochMilli());
                    healthStatusMessage.setReason("HEALTH CHECK");
                    healthStatusMessage.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
                    healthMessage.setPayload(healthStatusMessage.asJson().getBytes());
                    healthMessage.setQos(1);
                    client.publish(onboardingResponse.getConnectionCriteria().getCommands(), healthMessage);
                    healthStatusMessages.put(healthStatusMessage);
                } catch (MqttException e) {
                    log.error("Could not publish the health check message.", e);
                    throw new BusinessException(new ErrorMessage(ErrorKey.COULD_NOT_PUBLISH_HEALTH_MESSAGE, "Could not publish the health check message."));
                }
            } else {
                log.error("Could not publish the health check message. MQTT client is not connected.");
                throw new BusinessException(new ErrorMessage(ErrorKey.COULD_NOT_PUBLISH_HEALTH_MESSAGE, "Could not publish the health check message. MQTT client is not connected."));
            }
        }, () -> log.warn("Could not find or create a MQTT client for endpoint with the MQTT client ID '{}'.", onboardingResponse.getConnectionCriteria().getClientId()));
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
            log.warn("Health status message for endpoint ID {} has not been returned.", agrirouterEndpointId);
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
