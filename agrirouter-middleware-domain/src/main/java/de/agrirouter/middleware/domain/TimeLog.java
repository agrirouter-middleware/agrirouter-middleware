package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bson.Document;

/**
 * Telemetry data, stored in the document storage.
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class TimeLog extends NoSqlBaseEntity {

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
     * The original time log or device description.
     */
    private Document document;

    /**
     * The team set context id.
     */
    private String teamSetContextId;

}
