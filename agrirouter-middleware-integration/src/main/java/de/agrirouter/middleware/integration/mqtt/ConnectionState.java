package de.agrirouter.middleware.integration.mqtt;

import java.util.List;

/**
 * The state of a connection.
 *
 * @param clientId         The client ID that is used for the connection.
 * @param cached           Is the connection available and cached?
 * @param connected        Is the connection still connected to the AR?
 * @param connectionErrors The connections errors.
 */
public record ConnectionState(String clientId, boolean cached, boolean connected,
                              List<ConnectionError> connectionErrors) {
}
