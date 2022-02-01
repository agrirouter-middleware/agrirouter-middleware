package de.agrirouter.middleware.controller.dto.response;

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
@Schema(description = "The response when registering a machine.")
public class RegisterMachineResponse extends Response {

    /**
     * The team set context ID.
     */
    @Schema(description = "The team set context ID, provided by the agrirouter.")
    String teamSetContextId;
}
