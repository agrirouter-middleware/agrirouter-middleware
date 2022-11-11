package de.agrirouter.middleware.integration.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttClient;

import java.util.List;
import java.util.Optional;

/**
 * A single cached connection.
 *
 * @param agrirouterEndpointId The ID of the endpoint
 * @param id                   The ID of the entry.
 * @param mqttClient           The dedicated MQTT client.
 * @param connectionErrors     The list of connection errors.
 */
public record CachedMqttClient(String agrirouterEndpointId, String id,
                               Optional<IMqttClient> mqttClient,
                               List<ConnectionError> connectionErrors) {
    public void clearConnectionErrors() {
        connectionErrors.clear();
    }

}
