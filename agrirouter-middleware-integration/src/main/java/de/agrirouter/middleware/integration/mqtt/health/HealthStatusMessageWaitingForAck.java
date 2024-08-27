package de.agrirouter.middleware.integration.mqtt.health;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Message to be sent to the MQTT broker to check the health status of the connection.
 */
@Getter
@Setter
@Builder
public class HealthStatusMessageWaitingForAck {

    /**
     * The ID of the external endpoint.
     */
    private String agrirouterEndpointId;

    /**
     *
     */
    private String messageId;

    /**
     * Flag to indicate if the message has been returned from the MQTT broker.
     */
    private HealthStatus healthStatus;


}
