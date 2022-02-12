package de.agrirouter.middleware.controller.dto.request.messaging.enums;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The video type, used to map the specific agrirouter message type.
 */
@Schema(description = "The video type, used to map the specific agrirouter message type.")
public enum VideoType {

    AVI(ContentMessageType.VID_AVI),
    MP4(ContentMessageType.VID_MP4),
    WMV(ContentMessageType.VID_WMV);

    /**
     * The mapped content message type.
     */
    private final ContentMessageType contentMessageType;

    VideoType(ContentMessageType contentMessageType) {
        this.contentMessageType = contentMessageType;
    }

    public ContentMessageType getContentMessageType() {
        return contentMessageType;
    }
}
