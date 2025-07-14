package de.agrirouter.middleware.integration.mqtt.list_endpoints;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * One of the message recipients for the endpoint.
 */
@Getter
@Setter
@ToString
public class MessageRecipient {

    /**
     * The agrirouter© endpoint ID.
     */
    private String agrirouterEndpointId;

    /**
     * The name of the endpoint, defined by the user.
     */
    private String endpointName;

    /**
     * The type of the endpoint.
     */
    private String endpointType;

    /**
     * The external ID.
     */
    private String externalId;

    /**
     * The technical message type.
     */
    private String technicalMessageType;

    /**
     * The timestamp when the message recipients have been fetched.
     */
    private Instant timestamp;

}
