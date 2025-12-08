package de.agrirouter.middleware.domain.documents;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Telemetry data, stored in the document storage.
 */
@Data
@ToString
@Document
@EqualsAndHashCode(callSuper = true)
@CompoundIndexes({
        @CompoundIndex(name = "timestamp_teamSetContextId_idx", def = "{'timestamp': 1, 'teamSetContextId': 1}", background = true)
})
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
    @Indexed(name = "agrirouterEndpointId_idx", background = true)
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
    @Indexed(name = "teamSetContextId_idx", background = true)
    private String teamSetContextId;

}
