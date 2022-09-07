package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.CloudOnboardingFailureDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * Response class for better API design.
 */
@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "The response when asking for the cloud onboard failures for an endpoint.")
public class CloudOnboardingFailureResponse extends Response {

    @Schema(description = "The cloud onboard failure found for the request.")
    CloudOnboardingFailureDto cloudOnboardFailure;

}
