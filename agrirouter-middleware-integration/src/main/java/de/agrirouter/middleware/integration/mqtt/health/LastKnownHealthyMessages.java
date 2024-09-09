package de.agrirouter.middleware.integration.mqtt.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Container for the last known healthy status messages.
 */
@Slf4j
@Component
public class LastKnownHealthyMessages {

    private final Map<String, Instant> lastKnownHealthyStatus = new HashMap<>();

    /**
     * Get the last known healthy status for the given endpoint ID.
     *
     * @param agrirouterEndpointId The endpoint ID.
     */
    public void put(String agrirouterEndpointId) {
        lastKnownHealthyStatus.put(agrirouterEndpointId, Instant.now());
    }

    /**
     * Remove the last known healthy status for the given endpoint ID.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @return The last known healthy status.
     */
    public Optional<Instant> get(String agrirouterEndpointId) {
        var instant = lastKnownHealthyStatus.get(agrirouterEndpointId);
        if (instant == null) {
            log.warn("No last known healthy status found for endpoint ID {}.", agrirouterEndpointId);
        }
        return Optional.ofNullable(instant);
    }
}
