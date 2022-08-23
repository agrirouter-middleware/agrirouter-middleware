package de.agrirouter.middleware.controller.dto.response.domain.timelog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

/**
 * The time log with the raw data.
 */
@Getter
@Setter
@Schema(description = "The time log with the raw data.")
public class TimeLogWithRawDataDto {

    /**
     * The message ID.
     */
    @Schema(description = "The message ID.")
    private String messageId;

    /**
     * The timestamp of the message.
     */
    @Schema(description = "The timestamp of the message.")
    private long timestamp;

    /**
     * The ID of the receiver.
     */
    @Schema(description = "The ID of the receiver.")
    private String receiverId;

    /**
     * The ID of the sender.
     */
    @Schema(description = "The ID of the sender.")
    private String senderId;

    /**
     * The ID of the endpoint.
     */
    @Schema(description = "The ID of the endpoint.")
    private String agrirouterEndpointId;

    /**
     * The external ID of the endpoint.
     */
    @Schema(description = "The external ID of the endpoint.")
    private String externalEndpointId;

    /**
     * The original time log or device description.
     */
    @Schema(description = "The original time log or device description.")
    private Document document;

    /**
     * The team set context id.
     */
    @Schema(description = "The team set context id.")
    private String teamSetContextId;

}
