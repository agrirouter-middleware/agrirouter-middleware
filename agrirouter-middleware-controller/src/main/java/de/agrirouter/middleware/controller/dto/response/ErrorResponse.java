package de.agrirouter.middleware.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * Generic error response to map the internal errors and provide clean API description.
 */
@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Generic error response to map the internal errors and provide clean API description.")
public class ErrorResponse extends Response {

    @Schema(description = "The key of the error.")
    String key;

    @Schema(description = "The message of the error.")
    String errorMessage;

}
