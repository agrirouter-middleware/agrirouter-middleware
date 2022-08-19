package de.agrirouter.middleware.integration.mqtt;

import lombok.Value;

import java.util.List;

/**
 * The state of a connection.
 */
@Value
public class TechnicalConnectionState {
    /**
     * The client ID that is used for the connection.
     */
    String clientId;

    /**
     * The server URI.
     */
    String serverURI;

    /**
     * The number of pending delivery tokens.
     */
    int nrOfPendingDeliveryTokens;

    /**
     * The pending delivery tokens.
     */
    List<PendingDeliveryToken> pendingDeliveryTokens;

    /**
     * The connection errors.
     */
    List<ConnectionError> connectionErrors;
}
