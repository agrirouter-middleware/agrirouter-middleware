package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.integration.mqtt.health.HealthStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

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
     * an HTTP 503 if the endpoint has some problems or an HTTP 404 otherwise.
     */
    @Schema(description = "The status for a single endpoint, could be either a HTTP 200, if the endpoint is connected, a HTTP 503 if the endpoints has some problems or a HTTP 404 otherwise.")
    HealthStatus healthStatus;

}