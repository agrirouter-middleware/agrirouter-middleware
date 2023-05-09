package de.agrirouter.middleware.integration.mqtt;

import java.util.List;

/**
 * The state of a connection.
 *
 * @param nrOfPendingDeliveryTokens The number of pending delivery tokens.
 * @param pendingDeliveryTokens     The pending delivery tokens.
 * @param connectionErrors          The connection errors.
 */
public record TechnicalConnectionState(int nrOfPendingDeliveryTokens,
                                       List<PendingDeliveryToken> pendingDeliveryTokens,
                                       List<ConnectionError> connectionErrors) {

}
