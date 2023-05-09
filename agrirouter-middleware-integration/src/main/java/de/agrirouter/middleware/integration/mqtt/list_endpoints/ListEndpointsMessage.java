package de.agrirouter.middleware.integration.mqtt.list_endpoints;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * Message to be sent to the MQTT broker to fetch the recipients of the connected endpoints.
 */
@Getter
@Setter
public class ListEndpointsMessage {

    /**
     * The timestamp of the message.
     */
    private long timestamp;

    /**
     * The ID of the external endpoint.
     */
    private String agrirouterEndpointId;

    /**
     * Flag to indicate if the message has been returned from the MQTT broker.
     */
    private boolean hasBeenReturned = false;

    /**
     * The recipients of the connected endpoint.
     */
    private Collection<MessageRecipient> messageRecipients;

}
