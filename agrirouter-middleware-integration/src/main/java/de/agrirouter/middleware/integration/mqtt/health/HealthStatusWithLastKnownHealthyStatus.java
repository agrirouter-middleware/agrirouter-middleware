package de.agrirouter.middleware.integration.mqtt.health;

import lombok.Value;

import java.time.Instant;

/**
 * Container for the health status messages.
 */
@Value
public class HealthStatusWithLastKnownHealthyStatus {

    /**
     * The status for a single endpoint.
     */
    HealthStatus healthStatus;

    /**
     * The last known healthy status for the endpoint.
     */
    Instant lastKnownHealthyStatus;
}
