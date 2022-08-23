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
@Schema(description = "The warnings of an endpoint.")
public class EndpointWarningsDto extends CommonEndpointStatusDto {

    /**
     * The detailed warnings messages with timestamps.
     */
    @Schema(description = "The detailed warnings with timestamps.")
    private List<LogEntryDto> warnings;

}
