package de.agrirouter.middleware.integration.mqtt.health;

import agrirouter.response.Response;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Message to be sent to the MQTT broker to check the health status of the connection.
 */
@Getter
@Setter
@Builder
public class HealthStatusMessage {

    /**
     * The timestamp of the message.
     */
    private long timestamp;

    /**
     * The ID of the endpoint.
     */
    private String agrirouterEndpointId;

    /**
     * The ID of the message.
     */
    private String messageId;

    /**
     * Flag to indicate if the message has been returned from the MQTT broker.
     */
    private boolean hasBeenReturned;

    /**
     * Flag to indicate if the message has been returned from the MQTT broker.
     */
    private Response.ResponseEnvelope.ResponseBodyType healthStatus;


}
