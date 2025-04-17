package de.agrirouter.middleware.controller.dto.request.messaging;

import de.agrirouter.middleware.controller.dto.request.messaging.enums.ImageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request to publish image data.
 */
@Getter
@Setter
@ToString
@Schema(description = "Request to publish image data.")
public class PublishImageDataRequest extends PublishNonTelemetryDataRequest {

    /**
     * The type of image.
     */
    @NotNull
    @Schema(description = "The type of image.")
    private ImageType imageType;

}
