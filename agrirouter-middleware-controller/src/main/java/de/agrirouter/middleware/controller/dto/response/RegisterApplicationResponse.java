package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.ApplicationDto;
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
@Schema(description = "The response after an application has been successfully registered.")
public class RegisterApplicationResponse extends Response {

    /**
     * The application that was registered.
     */
    @Schema(description = "The application that was registered.")
    ApplicationDto application;

}
