package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

/**
 * The current state of the connection.
 */
@Data
@ToString
@Schema(description = "The current status of the connection.")
public class ConnectionStateDto {

    /**
     * Is the connection available and cached?
     */
    @Schema(description = "Is the connection available and cached?")
    private boolean cached;

    /**
     * Is the connection still connected to the AR?
     */
    @Schema(description = "Is the connection still connected to the AR?")
    private boolean connected;

    /**
     * The client ID of the connection.
     */
    @Schema(description = "The client ID of the connection.")
    private String clientId;

}
