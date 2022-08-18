package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.EndpointConnectionStatusDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.Map;

/**
 * Response class for better API design.
 */
@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "The response when asking for an endpoint connection status.")
public class EndpointConnectionStatusResponse extends Response {

    /**
     * The endpoints with their status.
     */
    @Schema(description = "The endpoints found for the request, each one represented by its ID and the belonging status.")
    Map<String, EndpointConnectionStatusDto> endpoints;

}
