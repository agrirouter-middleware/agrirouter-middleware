package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Base class for the private API controllers.
 */
@SecurityScheme(
        name = "monitoring",
        description = "Default monitoring scheme, used for the monitoring and statistics.",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
@SecurityRequirement(
        name = "monitoring-api"
)
public interface MonitoringApiController {

    String API_PREFIX = Routes.MonitoringEndpoints.ALL_REQUESTS;

}
