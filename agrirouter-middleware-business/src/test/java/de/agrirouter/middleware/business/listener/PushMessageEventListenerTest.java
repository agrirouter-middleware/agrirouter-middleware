package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PushMessageEventListenerTest {

    private final PushMessageEventListener pushMessageEventListener = new PushMessageEventListener(null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    @Test
    void givenIso11783Field_whenIsMasterData_thenReturnsTrue() {
        String technicalMessageType = TemporaryContentMessageType.ISO_11783_FIELD.getKey();
        boolean result = pushMessageEventListener.isMasterData(technicalMessageType);
        assertTrue(result);
    }

    @Test
    void givenIso11783Farm_whenIsMasterData_thenReturnsTrue() {
        String technicalMessageType = TemporaryContentMessageType.ISO_11783_FARM.getKey();
        boolean result = pushMessageEventListener.isMasterData(technicalMessageType);
        assertTrue(result);
    }

    @Test
    void givenIso11783Customer_whenIsMasterData_thenReturnsTrue() {
        String technicalMessageType = TemporaryContentMessageType.ISO_11783_CUSTOMER.getKey();
        boolean result = pushMessageEventListener.isMasterData(technicalMessageType);
        assertTrue(result);
    }

    @Test
    void givenNonMasterDataType_whenIsMasterData_thenReturnsFalse() {
        String technicalMessageType = "some:nonMasterDataType";
        boolean result = pushMessageEventListener.isMasterData(technicalMessageType);
        assertFalse(result);
    }
}