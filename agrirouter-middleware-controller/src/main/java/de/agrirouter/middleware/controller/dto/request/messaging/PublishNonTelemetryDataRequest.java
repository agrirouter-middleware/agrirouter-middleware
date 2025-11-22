package de.agrirouter.middleware.controller.dto.request.messaging;

import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Request to publish non-telemetry data like images, task data or something else.
 */
@Getter
@Setter
@ToString
@Schema(description = "Request to publish non-telemetry data.")
public class PublishNonTelemetryDataRequest {

    /**
     * The content message type to be published.
     */
    @Schema(description = "The content message type to be published.")
    private TemporaryContentMessageType contentMessageType;

    /**
     * The content of the message, should be Base64 encoded either way.
     */
    @NotNull
    @NotEmpty
    @Schema(description = "The message tuples.")
    private List<PublishNonTelemetryDataMessageTuple> messageTuples;

    /**
     * Internal class to encapsulate the message tuples.
     */
    @Getter
    @Setter
    @ToString
    @Schema(description = "Internal class to encapsulate the message tuples.")
    public static class PublishNonTelemetryDataMessageTuple {

        /**
         * The content of the message, should be Base64 encoded either way.
         */
        @NotNull
        @NotEmpty
        @Schema(description = "The Base64 encoded message content.")
        private String messageContent;

        /**
         * The name of the file.
         */
        @NotNull
        @NotEmpty
        @Schema(description = "The name of the file.")
        private String fileName;

        /**
         * The recipients for direct sending. If there is a recipient, the file will not be published.
         */
        @Schema(description = "The recipients for direct sending. If there is a recipient, the file will not be published.")
        private List<String> recipients;

    }

}
