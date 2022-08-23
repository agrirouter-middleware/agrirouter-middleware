package de.agrirouter.middleware.controller.dto.response.domain;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO.
 */
@Getter
@Setter
@Schema(description = "Holding the content of a file.")
public class FileHeaderDto {

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
     * Name of the file.
     */
    private String filename;

    /**
     * The ID for the chunking context.
     */
    private String chunkContextId;

    /**
     * The size of the payload.
     */
    private long payloadSize;

    /**
     * The ID of the sender.
     */
    private String senderId;

}
