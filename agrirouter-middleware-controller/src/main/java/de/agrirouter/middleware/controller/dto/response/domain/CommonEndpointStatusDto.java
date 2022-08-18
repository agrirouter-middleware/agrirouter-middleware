package de.agrirouter.middleware.controller.dto.response.domain;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * A common endpoint status.
 */
@Getter
@Setter
@Schema(description = "The common status of an endpoint.")
public class CommonEndpointStatusDto {

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

}
