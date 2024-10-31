package de.agrirouter.middleware.domain;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Metadata for content messages.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class ContentMessageMetadata extends BaseEntity {

    /**
     * The message ID.
     */
    private String messageId;

    /**
     * The technical message type.
     */
    private String technicalMessageType;

    /**
     * The timestamp of the message.
     */
    private long timestamp;

    /**
     * The ID of the receiver.
     */
    private String receiverId;

    /**
     * Name of the file.
     */
    private String filename;

    /**
     * The ID for the chunking context.
     */
    private String chunkContextId;

    /**
     * The current chunk.
     */
    private long currentChunk;

    /**
     * The number of total chunks.
     */
    private long totalChunks;

    /**
     * The total chunk size.
     */
    private long totalChunkSize;

    /**
     * The size of the payload.
     */
    private long payloadSize;

    /**
     * The ID of the sender.
     */
    private String senderId;

    /**
     * The sequence number of the message.
     */
    private long sequenceNumber;

    /**
     * The ID of the team set (for machines).
     */
    private String teamSetContextId;

}
