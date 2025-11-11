package de.agrirouter.middleware.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request DTO to migrate a single endpoint from one application to another within the same tenant.
 */
@Getter
@Setter
@ToString
@Schema(description = "Request to migrate a single endpoint from one application to another within the same tenant.")
public class MigrateEndpointRequest {

    @NotBlank
    @Schema(description = "The external endpoint ID of the endpoint to migrate.")
    private String externalEndpointId;

    @NotBlank
    @Schema(description = "The internal ID of the source application that currently owns the endpoint.")
    private String sourceInternalApplicationId;

    @NotBlank
    @Schema(description = "The internal ID of the target application to which the endpoint should be moved.")
    private String targetInternalApplicationId;
}
