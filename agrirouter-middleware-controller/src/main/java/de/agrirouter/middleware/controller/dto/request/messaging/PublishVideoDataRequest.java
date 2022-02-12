package de.agrirouter.middleware.controller.dto.request.messaging;

import de.agrirouter.middleware.controller.dto.request.messaging.enums.VideoType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Request to publish video data.
 */
@Getter
@Setter
@ToString
@Schema(description = "Request to publish image data.")
public class PublishVideoDataRequest extends PublishNonTelemetryDataRequest {

    /**
     * The type of video.
     */
    @NotNull
    @NotEmpty
    @Schema(description = "The type of video.")
    private VideoType videoType;

}
