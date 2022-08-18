package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * A single connection error.
 */
@Getter
@Setter
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
