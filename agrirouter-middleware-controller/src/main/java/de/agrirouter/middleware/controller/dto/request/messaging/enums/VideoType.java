package de.agrirouter.middleware.controller.dto.request.messaging.enums;

import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * The video type, used to map the specific agrirouter© message type.
 */
@Getter
@Schema(description = "The video type, used to map the specific agrirouter© message type.")
public enum VideoType {

    AVI(TemporaryContentMessageType.VID_AVI),
    MP4(TemporaryContentMessageType.VID_MP4),
    WMV(TemporaryContentMessageType.VID_WMV);

    /**
     * The mapped content message type.
     */
    private final TemporaryContentMessageType contentMessageType;

    VideoType(TemporaryContentMessageType contentMessageType) {
        this.contentMessageType = contentMessageType;
    }

}
