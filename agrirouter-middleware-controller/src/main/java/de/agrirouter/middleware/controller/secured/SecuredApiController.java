package de.agrirouter.middleware.controller.secured;

import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.controller.CommonController;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Base class for the private API controllers.
 */
@SecurityScheme(
        name = "secured",
        description = "Default security scheme, used for the private flows.",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
@SecurityRequirement(
        name = "secured-api"
)
interface SecuredApiController extends CommonController {

    String API_PREFIX = Routes.Secured.API_PATH;

}
