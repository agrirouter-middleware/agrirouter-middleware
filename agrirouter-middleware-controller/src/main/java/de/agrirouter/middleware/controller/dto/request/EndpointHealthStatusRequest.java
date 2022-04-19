package de.agrirouter.middleware.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Request for endpoint health status.
 */
@Getter
@Setter
@ToString
@Schema(description = "Check the status of multiple endpoints.")
public class EndpointHealthStatusRequest {

    /**
     * The IDs of the endpoints for the status check.
     */
    @Schema(description = "The IDs of the endpoints for the status check.")
    private List<String> externalEndpointIds;

}
