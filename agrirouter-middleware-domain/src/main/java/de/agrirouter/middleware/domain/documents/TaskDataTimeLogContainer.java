package de.agrirouter.middleware.domain.documents;

import de.agrirouter.middleware.domain.ContentMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Representing the time logs of a task data file.
 */
@Data
@ToString
@Document
@EqualsAndHashCode(callSuper = true)
public class TaskDataTimeLogContainer extends NoSqlBaseEntity {

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
    private String endpointId;

    /**
     * The original time log.
     */
    private List<org.bson.Document> timeLogs;

    public TaskDataTimeLogContainer(ContentMessage contentMessage, List<org.bson.Document> timeLogs) {
        this.setEndpointId(contentMessage.getAgrirouterEndpointId());
        this.setMessageId(contentMessage.getContentMessageMetadata().getMessageId());
        this.setTimestamp(contentMessage.getContentMessageMetadata().getTimestamp());
        this.setReceiverId(contentMessage.getContentMessageMetadata().getReceiverId());
        this.setSenderId(contentMessage.getContentMessageMetadata().getSenderId());
        this.setTimeLogs(timeLogs);
    }

}
