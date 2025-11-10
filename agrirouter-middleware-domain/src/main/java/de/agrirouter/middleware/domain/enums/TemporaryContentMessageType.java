package de.agrirouter.middleware.domain.enums;

import agrirouter.technicalmessagetype.Gps;
import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum TemporaryContentMessageType implements TechnicalMessageType {

    ISO_11783_TASKDATA_ZIP("iso:11783:-10:taskdata:zip", "", true),
    SHP_SHAPE_ZIP("shp:shape:zip", "", true),
    DOC_PDF("doc:pdf", "", true),
    IMG_JPEG("img:jpeg", "", true),
    IMG_PNG("img:png", "", true),
    IMG_BMP("img:bmp", "", true),
    VID_AVI("vid:avi", "", true),
    VID_MP4("vid:mp4", "", true),
    VID_WMV("vid:wmv", "", true),
    GPS_INFO("gps:info", Gps.GPSList.getDescriptor().getFullName(), false),

    ISO_11783_FIELD("masterdata:partfield", "iso11783p10.v0.m0.messages.Partfield", false),
    ISO_11783_FARM("masterdata:farm", "iso11783p10.v0.m0.messages.Farm", false),
    ISO_11783_CUSTOMER("masterdata:customer", "iso11783p10.v0.m0.messages.Customer", false),
    ISO_11783_DEVICE_DESCRIPTION(
            "iso:11783:-10:device_description:protobuf",
            "types.agrirouter.com/efdi.ISO11783_TaskData",
            false
    ),
    ISO_11783_TIME_LOG("iso:11783:-10:time_log:protobuf", "types.agrirouter.com/efdi.TimeLog", false);


    private final String key;
    private final String typeUrl;
    private final boolean needsBase64Encoding;

    private static final Map<String, TemporaryContentMessageType> LOOKUP_BY_KEY;

    static {
        java.util.Map<String, TemporaryContentMessageType> map = new HashMap<>();
        for (TemporaryContentMessageType t : values()) {
            map.put(t.key, t);
        }
        LOOKUP_BY_KEY = Collections.unmodifiableMap(map);
    }

    @Override
    public boolean needsBase64EncodingAndHasToBeChunkedIfNecessary() {
        return needsBase64Encoding;
    }

    /**
     * Resolves a {@link TemporaryContentMessageType} instance based on the provided key.
     *
     * @param key The unique key representing a specific {@link TemporaryContentMessageType}.
     *            It may be null or may not match any existing key.
     * @return The corresponding {@link TemporaryContentMessageType} if the provided key matches any key;
     * otherwise, returns null.
     */
    public static TemporaryContentMessageType fromKey(String key) {
        if (key == null) {
            return null;
        }
        return LOOKUP_BY_KEY.get(key);
    }
}