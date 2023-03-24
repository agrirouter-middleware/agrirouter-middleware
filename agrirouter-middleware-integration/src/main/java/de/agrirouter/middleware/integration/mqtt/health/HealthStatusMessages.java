package de.agrirouter.middleware.integration.mqtt.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Container for the health status messages.
 */
@Slf4j
@Component
public class HealthStatusMessages {

    private final Map<String, HealthStatusMessage> healthStatusMessages = new HashMap<>();

    /**
     * Place the health status message for the given endpoint ID within the container.
     *
     * @param healthStatusMessage The health status message.
     */
    public void put(HealthStatusMessage healthStatusMessage) {
        healthStatusMessages.put(healthStatusMessage.getAgrirouterEndpointId(), healthStatusMessage);
    }

    /**
     * Remove the health status message for the given endpoint ID from the container.
     *
     * @param agrirouterEndpointId The endpoint ID.
     */
    public void remove(String agrirouterEndpointId) {
        var healthStatusMessage = healthStatusMessages.remove(agrirouterEndpointId);
        if (healthStatusMessage == null) {
            log.warn("No health status message found for endpoint ID {}.", agrirouterEndpointId);
        }
    }

    /**
     * Get the health status message for the given endpoint ID.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @return The health status message.
     */
    public HealthStatusMessage get(String agrirouterEndpointId) {
        var healthStatusMessage = healthStatusMessages.get(agrirouterEndpointId);
        if (healthStatusMessage == null) {
            log.warn("No health status message found for endpoint ID {}.", agrirouterEndpointId);
        }
        return healthStatusMessage;
    }
}
