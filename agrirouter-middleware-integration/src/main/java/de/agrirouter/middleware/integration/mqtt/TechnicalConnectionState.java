package de.agrirouter.middleware.integration.mqtt;

import lombok.Value;

import java.util.List;

/**
 * The state of a connection.
 */
@Value
public class TechnicalConnectionState {

    /**
     * The number of pending delivery tokens.
     */
    int nrOfPendingDeliveryTokens;

    /**
     * True if the application is using router devices.
     */
    boolean usesRouterDevice;

    /**
     * The pending delivery tokens.
     */
    List<PendingDeliveryToken> pendingDeliveryTokens;

    /**
     * The connection errors.
     */
    List<ConnectionError> connectionErrors;
}
