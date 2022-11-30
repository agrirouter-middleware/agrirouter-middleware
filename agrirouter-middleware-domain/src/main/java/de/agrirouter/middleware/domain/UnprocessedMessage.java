package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;

/**
 * This is a unprocessed outbox message that was fetched from the outbox of the endpoint.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class UnprocessedMessage extends BaseEntity {

    /**
     * The endpoint ID.
     */
    @Column(nullable = false)
    private String agrirouterEndpointId;

    /**
     * The message itself.
     */
    @Lob
    @Column(nullable = false)
    private String message;

}
