package de.agrirouter.middleware.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
    @NotBlank
    @NotEmpty
    @Schema(description = "The list of external endpoint IDs.")
    private List<String> externalEndpointIds;

}
