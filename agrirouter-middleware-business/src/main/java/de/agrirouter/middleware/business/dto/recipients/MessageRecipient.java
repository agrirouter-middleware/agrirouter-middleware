package de.agrirouter.middleware.business.dto.recipients;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
     * The direction.
     */
    private String direction;

}
