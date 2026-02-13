package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * This is a unprocessed outbox message that was fetched from the outbox of the endpoint.
 */
@Data
@Document
@ToString
@EqualsAndHashCode(callSuper = true)
public class UnprocessedMessage extends BaseEntity {

    /**
     * The endpoint ID.
     */
    private String agrirouterEndpointId;

    /**
     * The message itself.
     */
    private String message;

}
