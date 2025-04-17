package de.agrirouter.middleware.controller.dto.request.messaging;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Request to publish telemetry data.
 */
@Getter
@Setter
@ToString
@Schema(description = "Request to publish telemetry data.")
public class PublishTimeLogDataRequest {

    /**
     * The messages themselves, should be Base64 encoded either way.
     */
    @NotNull
    @NotEmpty
    @Schema(description = "The Base64 encoded messages.")
    private List<String> base64EncodedMessages;

}
