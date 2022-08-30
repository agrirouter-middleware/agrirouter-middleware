package de.agrirouter.middleware.controller.unsecured;

import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.controller.CommonController;

/**
 * Base class for the public API controllers.
 */
interface UnsecuredApiController extends CommonController {

    String API_PREFIX = Routes.Unsecured.API_PATH;

}
