package de.agrirouter.middleware.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.time.Instant;

/**
 * Response class for better API design.
 */
@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "The response when asking for an endpoint health status.")
public class DetailedEndpointHealthStatusResponse extends Response {

    /**
     * The status for a single endpoint.
     * Could be either an HTTP 200 if the endpoint is connected,
     * an HTTP 400 if the endpoint has some problems or an HTTP 404 otherwise.
     */
    @Schema(description = "The status for a single endpoint, could be either a HTTP 200, if the endpoint is connected, a HTTP 503 if the endpoints has some problems or a HTTP 404 otherwise.")
    HttpStatus healthStatus;

    /**
     * The last known healthy status for the endpoint.
     */
    @Schema(description = "The last known healthy status for the endpoint, could be the same as the current status or a previous one.")
    Instant lastKnownHealthyStatus;

}
