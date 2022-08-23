package de.agrirouter.middleware.controller.dto.response.domain;

import de.agrirouter.middleware.domain.enums.EndpointType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The DTO for the representation of an endpoint.
 */
@Getter
@Setter
@Schema(description = "Representation of an endpoint with all children.")
public class EndpointWithChildrenDto {

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
     * The type of the endpoint.
     */
    @Schema(description = "The type of the endpoint.")
    private EndpointType endpointType;

    /**
     * All the children of the endpoint.
     */
    @Schema(description = "All the children of the endpoint.")
    private List<EndpointDto> connectedVirtualEndpoints;

    /**
     * Marks an endpoint as deactivated.
     */
    @Schema(description = "Marks an endpoint as deactivated.")
    private boolean deactivated;

    /**
     * The account ID for this endpoint.
     */
    @Schema(description = "The account ID for this endpoint.")
    private String agrirouterAccountId;

}
