package de.agrirouter.middleware.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * The request for the onboard process of a virtual endpoint.
 */
@Getter
@Setter
@ToString
@Schema(description = "The request for the onboard process of a virtual endpoint.")
public class OnboardVirtualEndpointRequest {

    /**
     * The ID of the new virtual endpoint within the middleware.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The ID of the new virtual endpoint within the middleware.")
    private String externalVirtualEndpointId;

    /**
     * The list of names for the virtual endpoints.
     */
    @NotNull
    @NotEmpty
    @Schema(description = "The list of names for the virtual endpoints.")
    private String endpointName;

}
