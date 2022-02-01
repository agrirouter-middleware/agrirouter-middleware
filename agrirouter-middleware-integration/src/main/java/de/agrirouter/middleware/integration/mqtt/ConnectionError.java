package de.agrirouter.middleware.integration.mqtt;

import lombok.Value;

import java.time.Instant;

/**
 * A single connection error.
 */
@Value
public class ConnectionError {

    /**
     * The point in time the connection error occured.
     */
    Instant pointInTime;

    /**
     * The error message.
     */
    String errorMessage;

}
