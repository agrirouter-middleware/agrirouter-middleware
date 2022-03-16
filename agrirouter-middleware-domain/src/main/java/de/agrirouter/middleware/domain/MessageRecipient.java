package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Entity;

/**
 * One of the message recipients for the endpoint.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class MessageRecipient extends BaseEntity {

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
