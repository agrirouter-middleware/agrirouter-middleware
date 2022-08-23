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
@Schema(description = "The connection status of an endpoint.")
public class EndpointConnectionStatusDto extends CommonEndpointStatusDto {

    /**
     * The detailed error messages with timestamps.
     */
    @Schema(description = "The detailed error messages with timestamps.")
    private List<ConnectionErrorDto> connectionErrors;

}
