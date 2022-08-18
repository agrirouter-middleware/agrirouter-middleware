package de.agrirouter.middleware.controller.dto.response.domain.timelog.periods;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;


/**
 * Telemetry data, stored in the document storage.
 */
@Getter
@Setter
@Schema(description = "Telemetry data, stored in the document storage.")
public class TimeLogDto {

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
     * The team set context id.
     */
    @Schema(description = "The team set context id.")
    private String teamSetContextId;

}
