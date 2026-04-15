package de.agrirouter.middleware.integration.mqtt.list_endpoints;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListEndpointsMessagesTest {

    private ListEndpointsMessages listEndpointsMessages;

    @BeforeEach
    void setUp() {
        listEndpointsMessages = new ListEndpointsMessages();
    }

    private ListEndpointsMessage buildMessage(String endpointId) {
        var msg = new ListEndpointsMessage();
        msg.setAgrirouterEndpointId(endpointId);
        msg.setTimestamp(System.currentTimeMillis());
        return msg;
    }

    @Test
    void put_andGet_returnsMessage() {
        var msg = buildMessage("ar-ep-1");

        listEndpointsMessages.put(msg);

        var result = listEndpointsMessages.get("ar-ep-1");
        assertThat(result).isNotNull();
        assertThat(result.getAgrirouterEndpointId()).isEqualTo("ar-ep-1");
    }

    @Test
    void get_withUnknownEndpointId_returnsNull() {
        var result = listEndpointsMessages.get("non-existent-ep");

        assertThat(result).isNull();
    }

    @Test
    void put_overwritesExistingEntryForSameEndpointId() {
        var msg1 = buildMessage("ar-ep-overwrite");
        msg1.setTimestamp(1000L);
        var msg2 = buildMessage("ar-ep-overwrite");
        msg2.setTimestamp(2000L);

        listEndpointsMessages.put(msg1);
        listEndpointsMessages.put(msg2);

        var result = listEndpointsMessages.get("ar-ep-overwrite");
        assertThat(result).isNotNull();
        assertThat(result.getTimestamp()).isEqualTo(2000L);
    }

    @Test
    void remove_deletesEntry() {
        var msg = buildMessage("ar-ep-remove");
        listEndpointsMessages.put(msg);

        listEndpointsMessages.remove("ar-ep-remove");

        assertThat(listEndpointsMessages.get("ar-ep-remove")).isNull();
    }

    @Test
    void remove_nonExistentId_doesNotThrow() {
        listEndpointsMessages.remove("no-such-endpoint");
        // Should not throw
    }

    @Test
    void put_multipleEntries_eachRetrievable() {
        listEndpointsMessages.put(buildMessage("ar-ep-X"));
        listEndpointsMessages.put(buildMessage("ar-ep-Y"));

        assertThat(listEndpointsMessages.get("ar-ep-X")).isNotNull();
        assertThat(listEndpointsMessages.get("ar-ep-Y")).isNotNull();
    }

    @Test
    void listEndpointsMessage_hasBeenReturned_defaultIsFalse() {
        var msg = buildMessage("ar-ep-default");

        assertThat(msg.isHasBeenReturned()).isFalse();
    }

    @Test
    void listEndpointsMessage_setHasBeenReturned_updatesFlag() {
        var msg = buildMessage("ar-ep-returned");
        msg.setHasBeenReturned(true);

        assertThat(msg.isHasBeenReturned()).isTrue();
    }

    @Test
    void listEndpointsMessage_setMessageRecipients_storesRecipients() {
        var msg = buildMessage("ar-ep-recipients");
        var recipient = new MessageRecipient();
        recipient.setAgrirouterEndpointId("recipient-ep-1");
        recipient.setEndpointName("Test Recipient");
        msg.setMessageRecipients(List.of(recipient));

        assertThat(msg.getMessageRecipients()).hasSize(1);
        assertThat(msg.getMessageRecipients().iterator().next().getAgrirouterEndpointId()).isEqualTo("recipient-ep-1");
    }
}
