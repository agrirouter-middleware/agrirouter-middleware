package de.agrirouter.middleware.controller.dto.response;


import de.agrirouter.middleware.controller.dto.response.domain.EndpointWithChildrenDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * Response class for better API design.
 */
@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "The response when searching for endpoints belonging to the application.")
public class FindEndpointsForApplicationResponse extends Response {

    /**
     * The endpoints.
     */
    @Schema(description = "The endpoints for this application.")
    List<EndpointWithChildrenDto> endpoints;

}
