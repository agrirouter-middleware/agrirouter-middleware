package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.ApplicationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * Response class for better API design.
 */
@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "The result after searching for one or multiple applications.")
public class FindApplicationsResponse extends Response {

    /**
     * The applications.
     */
    @Schema(description = "The applications matching the search.")
    List<ApplicationDto> applications;

}
