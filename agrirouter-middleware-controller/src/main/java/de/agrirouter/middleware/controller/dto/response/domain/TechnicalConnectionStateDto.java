package de.agrirouter.middleware.controller.dto.response.domain;

import de.agrirouter.middleware.integration.mqtt.TechnicalConnectionState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO.
 */
@Getter
@Setter
@Schema(description = "The errors of an endpoint.")
public class TechnicalConnectionStateDto extends CommonEndpointStatusDto {

    /**
     * The detailed technical connection state.
     */
    @Schema(description = "The detailed technical connection state.")
    private TechnicalConnectionState technicalConnectionState;

}
