package de.agrirouter.middleware.integration.mqtt.health;

import java.time.Instant;

/**
 * Container for the health status messages.
 *
 * @param healthStatus           The status for a single endpoint.
 * @param lastKnownHealthyStatus The last known healthy status for the endpoint.
 */
public record HealthStatusWithLastKnownHealthyStatus(HealthStatus healthStatus, Instant lastKnownHealthyStatus) {

}
