package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.time.Instant;

/**
 * A single connection error.
 */
@Data
@ToString
@Schema(description = "A single connection error, documented for the endpoint.")
public class ConnectionErrorDto {

    /**
     * The point in time the connection error occurred.
     */
    @Schema(description = "The point in time the connection error occurred.")
    private Instant pointInTime;

    /**
     * The error message.
     */
    @Schema(description = "The error message.")
    private String errorMessage;
}
