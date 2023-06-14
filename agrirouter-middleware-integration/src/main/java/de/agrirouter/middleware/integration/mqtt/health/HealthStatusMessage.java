package de.agrirouter.middleware.integration.mqtt.health;

import lombok.Getter;
import lombok.Setter;

/**
 * Message to be sent to the MQTT broker to check the health status of the connection.
 */
@Getter
@Setter
public class HealthStatusMessage {

    public static final String MESSAGE_PREFIX = "d.a.m.health";

    /**
     * The ID of the external endpoint.
     */
    private String agrirouterEndpointId;

    /**
     * Flag to indicate if the message has been returned from the MQTT broker.
     */
    private boolean hasBeenReturned = false;

    /**
     * Get the JSON representation of the message.
     *
     * @return The JSON representation.
     */
    public String asJson() {
        return MESSAGE_PREFIX + " {\"agrirouterEndpointId\":\"" + agrirouterEndpointId + "\"}";
    }

}
