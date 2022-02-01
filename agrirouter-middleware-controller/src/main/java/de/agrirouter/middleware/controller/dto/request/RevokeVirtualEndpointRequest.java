package de.agrirouter.middleware.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The request for the offboarding process of a virtual endpoint.
 */
@Getter
@Setter
@ToString
@Schema(description = "The request for the offboarding process of a virtual endpoint.")
public class RevokeVirtualEndpointRequest {

    /**
     * The list of endpoint IDs for the virtual endpoints.
     */
    @NotNull
    @Schema(description = "The list of endpoint IDs for the virtual endpoints.")
    private List<String> externalEndpointIds;

}
