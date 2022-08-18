package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.ApplicationWithEndpointStatusDto;
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
@Schema(description = "Response class to show the current status of an application.")
public class ApplicationStatusResponse extends Response {

    /**
     * The application incl. the endpoints and status information.
     */
    @Schema(description = "The current status of the application incl. its endpoints.")
    ApplicationWithEndpointStatusDto application;

}
