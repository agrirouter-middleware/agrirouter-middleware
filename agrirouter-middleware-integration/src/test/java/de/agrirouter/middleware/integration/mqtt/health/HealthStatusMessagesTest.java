package de.agrirouter.middleware.integration.mqtt.health;

import agrirouter.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HealthStatusMessagesTest {

    private HealthStatusMessages healthStatusMessages;

    @BeforeEach
    void setUp() {
        healthStatusMessages = new HealthStatusMessages();
    }

    private HealthStatusMessage buildMessage(String endpointId, String messageId) {
        return HealthStatusMessage.builder()
                .agrirouterEndpointId(endpointId)
                .messageId(messageId)
                .timestamp(System.currentTimeMillis())
                .hasBeenReturned(false)
                .healthStatus(null)
                .build();
    }

    @Test
    void put_andGet_returnsMessage() {
        var message = buildMessage("ar-ep-1", "msg-id-1");

        healthStatusMessages.put(message);

        var result = healthStatusMessages.get("ar-ep-1");
        assertThat(result).isPresent();
        assertThat(result.get().getAgrirouterEndpointId()).isEqualTo("ar-ep-1");
        assertThat(result.get().getMessageId()).isEqualTo("msg-id-1");
    }

    @Test
    void get_withUnknownEndpointId_returnsEmpty() {
        var result = healthStatusMessages.get("non-existent-id");

        assertThat(result).isEmpty();
    }

    @Test
    void put_overwritesExistingEntryForSameEndpointId() {
        var message1 = buildMessage("ar-ep-overwrite", "msg-id-original");
        var message2 = buildMessage("ar-ep-overwrite", "msg-id-updated");

        healthStatusMessages.put(message1);
        healthStatusMessages.put(message2);

        var result = healthStatusMessages.get("ar-ep-overwrite");
        assertThat(result).isPresent();
        assertThat(result.get().getMessageId()).isEqualTo("msg-id-updated");
    }

    @Test
    void remove_deletesEntry() {
        var message = buildMessage("ar-ep-remove", "msg-id-remove");
        healthStatusMessages.put(message);

        healthStatusMessages.remove("ar-ep-remove");

        assertThat(healthStatusMessages.get("ar-ep-remove")).isEmpty();
    }

    @Test
    void remove_nonExistentId_doesNotThrow() {
        healthStatusMessages.remove("non-existent-endpoint");
        // Should not throw
    }

    @Test
    void getByMessageId_returnsCorrectMessage() {
        var message = buildMessage("ar-ep-by-msg-id", "target-msg-id");
        healthStatusMessages.put(message);

        var result = healthStatusMessages.getByMessageId("target-msg-id");

        assertThat(result).isPresent();
        assertThat(result.get().getAgrirouterEndpointId()).isEqualTo("ar-ep-by-msg-id");
    }

    @Test
    void getByMessageId_withUnknownMessageId_returnsEmpty() {
        var result = healthStatusMessages.getByMessageId("unknown-message-id");

        assertThat(result).isEmpty();
    }

    @Test
    void getByMessageId_withMultipleEntries_returnsCorrectOne() {
        var msg1 = buildMessage("ar-ep-alpha", "msg-alpha");
        var msg2 = buildMessage("ar-ep-beta", "msg-beta");
        healthStatusMessages.put(msg1);
        healthStatusMessages.put(msg2);

        var result = healthStatusMessages.getByMessageId("msg-beta");

        assertThat(result).isPresent();
        assertThat(result.get().getAgrirouterEndpointId()).isEqualTo("ar-ep-beta");
    }

    @Test
    void healthStatusMessage_builder_setsAllFields() {
        var message = HealthStatusMessage.builder()
                .agrirouterEndpointId("ep-id")
                .messageId("m-id")
                .timestamp(12345L)
                .hasBeenReturned(true)
                .healthStatus(Response.ResponseEnvelope.ResponseBodyType.ACK)
                .build();

        assertThat(message.getAgrirouterEndpointId()).isEqualTo("ep-id");
        assertThat(message.getMessageId()).isEqualTo("m-id");
        assertThat(message.getTimestamp()).isEqualTo(12345L);
        assertThat(message.isHasBeenReturned()).isTrue();
        assertThat(message.getHealthStatus()).isEqualTo(Response.ResponseEnvelope.ResponseBodyType.ACK);
    }
}
