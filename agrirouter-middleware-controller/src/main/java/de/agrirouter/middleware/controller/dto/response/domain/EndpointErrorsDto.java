package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO.
 */
@Getter
@Setter
@Schema(description = "The errors of an endpoint.")
public class EndpointErrorsDto extends CommonEndpointStatusDto {

    /**
     * The detailed warnings messages with timestamps.
     */
    @Schema(description = "The detailed errors with timestamps.")
    private List<LogEntryDto> errors;

}
