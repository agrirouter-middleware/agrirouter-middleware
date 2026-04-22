package de.agrirouter.middleware.integration.ack;

import de.agrirouter.middleware.domain.Endpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MessageWaitingForAcknowledgementServiceTest {

    private MessageWaitingForAcknowledgementService service;

    @BeforeEach
    void setUp() {
        service = new MessageWaitingForAcknowledgementService();
        // Clear the static map between tests by removing any entries added
        // We use unique IDs per test to avoid cross-test contamination
    }

    private MessageWaitingForAcknowledgement buildMessage(String messageId, String endpointId) {
        var msg = new MessageWaitingForAcknowledgement();
        msg.setMessageId(messageId);
        msg.setAgrirouterEndpointId(endpointId);
        msg.setTechnicalMessageType("iso:11783:-10:time_log:protobuf");
        return msg;
    }

    @Test
    void save_andFindByMessageId_returnsMessage() {
        var messageId = UUID.randomUUID().toString();
        var msg = buildMessage(messageId, "ar-ep-save-test");

        service.save(msg);

        var result = service.findByMessageId(messageId);
        assertThat(result).isPresent();
        assertThat(result.get().getMessageId()).isEqualTo(messageId);
    }

    @Test
    void findByMessageId_withUnknownId_returnsEmpty() {
        var result = service.findByMessageId(UUID.randomUUID().toString());

        assertThat(result).isEmpty();
    }

    @Test
    void delete_removesMessageFromStore() {
        var messageId = UUID.randomUUID().toString();
        var msg = buildMessage(messageId, "ar-ep-delete-test");
        service.save(msg);

        service.delete(msg);

        assertThat(service.findByMessageId(messageId)).isEmpty();
    }

    @Test
    void delete_nonExistentMessage_doesNotThrow() {
        var msg = buildMessage(UUID.randomUUID().toString(), "ar-ep-no-op");

        // Should not throw even though the message was never saved
        service.delete(msg);
    }

    @Test
    void findAllForAgrirouterEndpointId_returnOnlyMatchingMessages() {
        var endpointId = "ar-ep-" + UUID.randomUUID();
        var otherId = "ar-ep-other-" + UUID.randomUUID();

        var msg1 = buildMessage(UUID.randomUUID().toString(), endpointId);
        var msg2 = buildMessage(UUID.randomUUID().toString(), endpointId);
        var msg3 = buildMessage(UUID.randomUUID().toString(), otherId);

        service.save(msg1);
        service.save(msg2);
        service.save(msg3);

        var results = service.findAllForAgrirouterEndpointId(endpointId);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(m -> m.getAgrirouterEndpointId().equals(endpointId));

        // cleanup
        service.delete(msg1);
        service.delete(msg2);
        service.delete(msg3);
    }

    @Test
    void findAllForAgrirouterEndpointId_whenNoneMatch_returnsEmpty() {
        var results = service.findAllForAgrirouterEndpointId("ar-ep-no-match-" + UUID.randomUUID());

        assertThat(results).isEmpty();
    }

    @Test
    void clearAllThatAreOlderThanOneWeek_removesOnlyOldMessages() {
        var endpointId = "ar-ep-clear-old-" + UUID.randomUUID();

        var freshMessage = buildMessage(UUID.randomUUID().toString(), endpointId);
        var oldMessage = buildMessage(UUID.randomUUID().toString(), endpointId);
        var eightDaysAgo = Instant.now().minusSeconds(60L * 60 * 24 * 8).getEpochSecond();
        ReflectionTestUtils.setField(oldMessage, "created", eightDaysAgo);

        service.save(freshMessage);
        service.save(oldMessage);

        service.clearAllThatAreOlderThanOneWeek();

        assertThat(service.findByMessageId(freshMessage.getMessageId())).isPresent();
        assertThat(service.findByMessageId(oldMessage.getMessageId())).isEmpty();

        // cleanup
        service.delete(freshMessage);
    }

    @Test
    void clearAllThatAreOlderThanOneWeek_whenNoOldMessages_doesNotRemoveFreshOnes() {
        var endpointId = "ar-ep-no-old-" + UUID.randomUUID();
        var msg = buildMessage(UUID.randomUUID().toString(), endpointId);
        service.save(msg);

        service.clearAllThatAreOlderThanOneWeek();

        assertThat(service.findByMessageId(msg.getMessageId())).isPresent();

        // cleanup
        service.delete(msg);
    }

    @Test
    void deleteAllForEndpoint_removesAllMessagesForThatEndpoint() {
        var endpointId = "ar-ep-delete-all-" + UUID.randomUUID();
        var otherId = "ar-ep-other-del-" + UUID.randomUUID();

        var msg1 = buildMessage(UUID.randomUUID().toString(), endpointId);
        var msg2 = buildMessage(UUID.randomUUID().toString(), endpointId);
        var msg3 = buildMessage(UUID.randomUUID().toString(), otherId);

        service.save(msg1);
        service.save(msg2);
        service.save(msg3);

        var endpoint = new Endpoint();
        endpoint.setAgrirouterEndpointId(endpointId);
        service.deleteAllForEndpoint(endpoint);

        assertThat(service.findByMessageId(msg1.getMessageId())).isEmpty();
        assertThat(service.findByMessageId(msg2.getMessageId())).isEmpty();
        assertThat(service.findByMessageId(msg3.getMessageId())).isPresent();

        // cleanup
        service.delete(msg3);
    }

    @Test
    void save_overwritesExistingEntryWithSameMessageId() {
        var messageId = UUID.randomUUID().toString();
        var msg1 = buildMessage(messageId, "ar-ep-overwrite-a");
        var msg2 = buildMessage(messageId, "ar-ep-overwrite-b");

        service.save(msg1);
        service.save(msg2);

        var result = service.findByMessageId(messageId);
        assertThat(result).isPresent();
        assertThat(result.get().getAgrirouterEndpointId()).isEqualTo("ar-ep-overwrite-b");

        // cleanup
        service.delete(msg2);
    }
}
