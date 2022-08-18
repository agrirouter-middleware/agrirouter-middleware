package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.EndpointErrorsDto;
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
@Schema(description = "The response when asking for the errors of an endpoint.")
public class EndpointErrorsResponse extends Response {

    /**
     * The endpoints with their status.
     */
    @Schema(description = "The endpoints found for the request, each one represented by its ID and their belonging errors .")
    Map<String, EndpointErrorsDto> endpoints;

}
