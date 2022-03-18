package de.agrirouter.middleware.integration.mqtt;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.eclipse.paho.client.mqttv3.IMqttClient;

import java.util.List;
import java.util.Optional;

/**
 * A single cached connection.
 */
@Value
@AllArgsConstructor
public class CachedMqttClient {

    /**
     * The ID of the endpoint
     */
    String agrirouterEndpointId;

    /**
     * The ID of the entry.
     */
    String id;

    /**
     * The dedicated MQTT client.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<IMqttClient> mqttClient;

    /**
     * The list of connection errors.
     */
    List<ConnectionError> connectionErrors;

}
