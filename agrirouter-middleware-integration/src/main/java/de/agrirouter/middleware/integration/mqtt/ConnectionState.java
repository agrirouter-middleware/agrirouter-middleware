package de.agrirouter.middleware.integration.mqtt;

import lombok.Value;

import java.util.List;

/**
 * The state of a connection.
 */
@Value
public class ConnectionState {

    /**
     * The client ID that is used for the connection.
     */
    String clientId;

    /**
     * Is the connection available and cached?
     */
    boolean cached;

    /**
     * Is the connection still connected to the AR?
     */
    boolean connected;

    /**
     * The connections errors.
     */
    List<ConnectionError> connectionErrors;

}
