package de.agrirouter.middleware.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * Parameters to create an endpoint.
 */
@Setter
@Getter
@ToString
@Schema(description = "Parameters to create an endpoint within the middleware and within the application.")
public class OnboardProcessRequest {

    /**
     * The registration code used for the request.
     */
    @NotBlank
    @Schema(description = "The registration code used for the request.")
    private String registrationCode;

}
