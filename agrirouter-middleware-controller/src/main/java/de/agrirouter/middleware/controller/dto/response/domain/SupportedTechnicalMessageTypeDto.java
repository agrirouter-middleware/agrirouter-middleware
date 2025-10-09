package de.agrirouter.middleware.controller.dto.response.domain;

import agrirouter.request.payload.endpoint.Capabilities;
import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO.
 */
@Getter
@Setter
@Schema(description = "A container holding the basic representation of a supported technical message type.")
public class SupportedTechnicalMessageTypeDto {

    /**
     * The technical message type, that the application does support, i.e. TaskData, EFDI, etc.
     */
    @Schema(description = "The technical message type, that the application does support, i.e. TaskData, EFDI, etc.")
    private TemporaryContentMessageType technicalMessageType;

    /**
     * The direction the message type can be handled, i.e. SEND, RECEIVE, SEND_RECEIVE.
     */
    @Schema(description = "The direction the message type can be handled, i.e. SEND, RECEIVE, SEND_RECEIVE.")
    private Capabilities.CapabilitySpecification.Direction direction;
}
