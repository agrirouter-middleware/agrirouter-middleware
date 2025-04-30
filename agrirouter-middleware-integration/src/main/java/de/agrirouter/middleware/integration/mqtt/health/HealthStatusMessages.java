package de.agrirouter.middleware.integration.mqtt.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        healthStatusMessages.remove(agrirouterEndpointId);
    }

    /**
     * Get the health status message for the given endpoint ID.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @return The health status message.
     */
    public Optional<HealthStatusMessage> get(String agrirouterEndpointId) {
        return Optional.ofNullable(healthStatusMessages.get(agrirouterEndpointId));
    }

    public Optional<HealthStatusMessage> getByMessageId(String messageId) {
        return healthStatusMessages.values().stream()
                .filter(healthStatusMessage -> healthStatusMessage.getMessageId().equals(messageId))
                .findFirst();
    }

}
