package de.agrirouter.middleware.integration.mqtt.health;

import org.springframework.http.HttpStatus;

import java.time.Instant;

/**
 * Container for the health status messages.
 *
 * @param healthStatus           The status for a single endpoint.
 * @param lastKnownHealthyStatus The last known healthy status for the endpoint.
 */
public record HealthStatusWithLastKnownHealthyStatus(HttpStatus healthStatus, Instant lastKnownHealthyStatus) {

}
