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
@Schema(description = "The missing acknowledgements of an endpoint.")
public class MissingAcknowledgementsDto extends CommonEndpointStatusDto {


    /**
     * The messages currently waiting for ACK.
     */
    @Schema(description = "The messages currently waiting for ACK.")
    private List<MessageWaitingForAcknowledgementDto> messagesWaitingForAck;

}
