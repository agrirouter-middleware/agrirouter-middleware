package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * DTO.
 */
@Data
@ToString
@Schema(description = "The connection status of an endpoint.")
public class EndpointConnectionStatusDto {

    /**
     * The ID of an endpoint.
     */
    @Schema(description = "The ID of an endpoint.")
    private String agrirouterEndpointId;

    /**
     * The external ID of an endpoint.
     */
    @Schema(description = "The external ID of an endpoint.")
    private String externalEndpointId;

    /**
     * The state of the connection.
     */
    @Schema(description = "The state of the connection.")
    private ConnectionStateDto connectionState;

    /**
     * The detailed error messages with timestamps.
     */
    @Schema(description = "The detailed error messages with timestamps.")
    private List<ConnectionErrorDto> connectionErrors;

}
