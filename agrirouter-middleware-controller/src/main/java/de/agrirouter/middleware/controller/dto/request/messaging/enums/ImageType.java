package de.agrirouter.middleware.controller.dto.request.messaging.enums;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * The image type, used to map the specific agrirouter© message type.
 */
@Getter
@Schema(description = "The image type, used to map the specific agrirouter© message type.")
public enum ImageType {

    JPEG(ContentMessageType.IMG_JPEG),
    PNG(ContentMessageType.IMG_PNG),
    BMP(ContentMessageType.IMG_BMP);

    /**
     * The mapped content message type.
     */
    private final ContentMessageType contentMessageType;

    ImageType(ContentMessageType contentMessageType) {
        this.contentMessageType = contentMessageType;
    }

}
