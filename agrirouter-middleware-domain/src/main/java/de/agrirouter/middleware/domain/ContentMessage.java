package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * This is a content message that was fetched from the outbox of the endpoint.
 */
@Data
@Document
@ToString
@EqualsAndHashCode(callSuper = true)
public class ContentMessage extends BaseEntity {

    /**
     * The endpoint ID.
     */
    private String agrirouterEndpointId;

    /**
     * The message itself.
     */
    private byte[] messageContent;

    /**
     * The metadata for a content message.
     */
    private ContentMessageMetadata contentMessageMetadata;

}
