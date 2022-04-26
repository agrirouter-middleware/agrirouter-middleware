package de.agrirouter.middleware.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Response class for better API design.
 */
@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "The response when asking for an endpoint health status.")
public class EndpointHealthStatusResponse extends Response {

    /**
     * The status for each of the endpoints.
     */
    @Schema(description = "The status for each of the endpoints. This would be either a HTTP 200, if the endpoint is connected, a HTTP 503 if the endpoints has some problems or a HTTP 404 otherwise.")
    Map<String, Integer> endpointStatus;

}
