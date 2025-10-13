package de.agrirouter.middleware.domain.documents;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Farm data, stored in the document storage.
 */
@Data
@ToString
@Document
@EqualsAndHashCode(callSuper = true)
public class Farm extends NoSqlBaseEntity {

    /**
     * The message ID.
     */
    private String messageId;

    /**
     * The timestamp of the message.
     */
    private long timestamp;

    /**
     * The ID of the receiver.
     */
    private String receiverId;

    /**
     * The ID of the sender.
     */
    private String senderId;

    /**
     * The ID of the endpoint.
     */
    private String agrirouterEndpointId;

    /**
     * The external ID of the endpoint.
     */
    private String externalEndpointId;

    /**
     * The ID of the farm.
     */
    private String farmId;

    /**
     * The original time log or device description.
     */
    private org.bson.Document document;

}
