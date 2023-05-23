package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;

/**
 * Base class for the public API controllers.
 */
public interface UnsecuredApiController {

    String API_PREFIX = Routes.UnsecuredEndpoints.ALL_REQUESTS;

}
