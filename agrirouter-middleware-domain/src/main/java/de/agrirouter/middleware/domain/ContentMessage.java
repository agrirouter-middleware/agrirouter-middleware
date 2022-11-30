package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.persistence.*;

/**
 * This is a content message that was fetched from the outbox of the endpoint.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class ContentMessage extends BaseEntity {

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
    private byte[] messageContent;

    /**
     * The metadata for a content message.
     */
    @JoinColumn
    @OneToOne(cascade = CascadeType.ALL)
    private ContentMessageMetadata contentMessageMetadata;

}
