package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * DTO.
 */
@Getter
@Setter
@Schema(description = "A log entry for the endpoint.")
public class LogEntryDto {

    /**
     * The response code.
     */
    @Schema(description = "The response code.")
    private int responseCode;

    /**
     * The message of the error.
     */
    @Schema(description = "The message of the error.")
    private String message;

    /**
     * The timestamp of the log.
     */
    @Schema(description = "The timestamp of the log.")
    private Date timestamp;

    /**
     * The type of the response.
     */
    @Schema(description = "The type of the response.")
    private String responseType;

    /**
     * The ID of the message.
     */
    @Schema(description = "The ID of the message.")
    private String messageId;

}
