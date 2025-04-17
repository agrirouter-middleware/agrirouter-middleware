package de.agrirouter.middleware.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * The request to fetch the status for multiple endpoints.
 */
@Getter
@Setter
@ToString
@Schema(description = "The request to fetch the status for multiple endpoints.")
public class EndpointStatusRequest {

    /**
     * The IDs of the endpoints.
     */
    @NotNull
    @NotEmpty
    @Schema(description = "The list of external endpoint IDs.")
    private List<String> externalEndpointIds;

}
