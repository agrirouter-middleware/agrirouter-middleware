package de.agrirouter.middleware.controller.dto.request;

import agrirouter.request.payload.endpoint.Capabilities;
import com.dke.data.agrirouter.api.enums.ContentMessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Add a supported technical message type to the request body.
 */
@Getter
@Setter
@ToString
@Schema(description = "Add a supported technical message type to the application.")
public class AddSupportedTechnicalMessageTypeRequest {

    /**
     * The representation of a supported technical message type.
     */
    @Getter
    @Setter
    @ToString
    @Schema(description = "Representation of a technical message type to add.")
    public static class SupportedTechnicalMessageTypeDto {

        /**
         * The technical message type, that the application does support, i.e. TaskData, EFDI, etc.
         */
        @NotNull
        @Schema(description = "The technical message type, that the application does support, i.e. TaskData, EFDI, etc.")
        private ContentMessageType technicalMessageType;

        /**
         * The direction the message type can be handled, i.e. SEND, RECEIVE, SEND_RECEIVE.
         */
        @NotNull
        @Schema(description = "The direction the message type can be handled, i.e. SEND, RECEIVE, SEND_RECEIVE.")
        private Capabilities.CapabilitySpecification.Direction direction;

        public ContentMessageType getTechnicalMessageType() {
            return technicalMessageType;
        }

    }

    /**
     * The list of supported technical message types.
     */
    @NotNull
    @NotEmpty
    @Schema(description = "The list of supported technical message types.")
    private List<@Valid SupportedTechnicalMessageTypeDto> supportedTechnicalMessageTypes;

}
