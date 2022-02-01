package de.agrirouter.middleware.controller.dto.response.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The result of the onboard process.
 */
@Schema(description = "The result of the onboard process.")
public enum OnboardProcessResult {

    SUCCESS, FAILURE

}
