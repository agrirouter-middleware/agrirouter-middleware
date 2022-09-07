package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * The cloud onboard failure.
 */
@Getter
@Setter
@Schema(description = "The common status of an endpoint.")
public class CloudOnboardingFailureDto {


    /**
     * The time of the failure.
     */
    @Schema(description = "The time of the failure.")
    Instant timestamp;

    /**
     * The external ID of the parent endpoint.
     */
    @Schema(description = "The external ID of the parent endpoint.")
    String externalEndpointId;

    /**
     * The ID of the virtual endpoint.
     */
    @Schema(description = "The ID of the virtual endpoint.")
    String virtualExternalEndpointId;

    /**
     * The error code.
     */
    @Schema(description = "The error code.")
    String errorCode;

    /**
     * The error message.
     */
    @Schema(description = "The error message.")
    String errorMessage;
}
