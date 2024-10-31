package de.agrirouter.middleware.domain.documents;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Telemetry data, stored in the document storage.
 */
@Data
@ToString
@Document
@EqualsAndHashCode(callSuper = true)
public class DeviceDescription extends NoSqlBaseEntity {

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
    private org.bson.Document document;

    /**
     * The team set context id.
     */
    private String teamSetContextId;

    /**
     * Shows whether the device description is deactivated or not.
     */
    private boolean deactivated;

    /**
     * The original Base64 encoded device description.
     */
    private String base64EncodedDeviceDescription;

}
