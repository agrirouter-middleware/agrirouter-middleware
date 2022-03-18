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
@Schema(description = "The response when searching for an application.")
public class FindApplicationResponse extends Response {

    /**
     * The application.
     */
    @Schema(description = "The application found for the request.")
    ApplicationDto application;

}
