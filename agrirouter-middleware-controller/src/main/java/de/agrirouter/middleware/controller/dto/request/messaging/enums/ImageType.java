package de.agrirouter.middleware.controller.dto.request.messaging.enums;

import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * The image type, used to map the specific agrirouter© message type.
 */
@Getter
@Schema(description = "The image type, used to map the specific agrirouter© message type.")
public enum ImageType {

    JPEG(TemporaryContentMessageType.IMG_JPEG),
    PNG(TemporaryContentMessageType.IMG_PNG),
    BMP(TemporaryContentMessageType.IMG_BMP);

    /**
     * The mapped content message type.
     */
    private final TemporaryContentMessageType contentMessageType;

    ImageType(TemporaryContentMessageType contentMessageType) {
        this.contentMessageType = contentMessageType;
    }

}
