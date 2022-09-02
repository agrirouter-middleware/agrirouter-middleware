package de.agrirouter.middleware.integration.mqtt;

import java.time.Instant;

/**
 * A single connection error.
 *
 * @param pointInTime  The point in time the connection error occurred.
 * @param errorMessage The error message.
 */
@SuppressWarnings("unused")
public record ConnectionError(Instant pointInTime, String errorMessage) {
}
