package de.agrirouter.middleware.business.enums;

import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TemporaryContentMessagesType implements TechnicalMessageType {

    public static final TemporaryContentMessagesType ISO_11783_FIELD = new TemporaryContentMessagesType("masterdata:partfield", "iso11783p10.v0.m0.messages.Partfield", false);
    public static final TemporaryContentMessagesType ISO_11783_FARM = new TemporaryContentMessagesType("masterdata:farm", "iso11783p10.v0.m0.messages.Farm", false);
    public static final TemporaryContentMessagesType ISO_11783_CUSTOMER = new TemporaryContentMessagesType("masterdata:customer", "iso11783p10.v0.m0.messages.Customer", false);

    private final String key;
    private final String typeUrl;
    private final boolean needsBase64Encoding;

    @Override
    public boolean needsBase64EncodingAndHasToBeChunkedIfNecessary() {
        return needsBase64Encoding;
    }
}
