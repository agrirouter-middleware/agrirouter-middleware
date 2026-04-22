package de.agrirouter.middleware.integration.ack;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MessageWaitingForAcknowledgementTest {

    @Test
    void created_isSetToCurrentTime() {
        var before = Instant.now().getEpochSecond();
        var message = new MessageWaitingForAcknowledgement();
        var after = Instant.now().getEpochSecond();

        assertThat(message.getCreated()).isBetween(before, after);
    }

    @Test
    void getDynamicPropertyAsString_withExistingKey_returnsValue() {
        var message = new MessageWaitingForAcknowledgement();
        message.getDynamicProperties().put(DynamicMessageProperties.TEAM_SET_CONTEXT_ID, "context-123");

        var result = message.getDynamicPropertyAsString(DynamicMessageProperties.TEAM_SET_CONTEXT_ID);

        assertThat(result).isEqualTo("context-123");
    }

    @Test
    void getDynamicPropertyAsString_withMissingKey_returnsNull() {
        var message = new MessageWaitingForAcknowledgement();

        var result = message.getDynamicPropertyAsString("non-existent-key");

        assertThat(result).isNull();
    }

    @Test
    void getDynamicPropertyAsStringList_withExistingKey_returnsList() {
        var message = new MessageWaitingForAcknowledgement();
        var ids = List.of("ep-1", "ep-2", "ep-3");
        message.getDynamicProperties().put(DynamicMessageProperties.EXTERNAL_VIRTUAL_ENDPOINT_IDS, ids);

        var result = message.getDynamicPropertyAsStringList(DynamicMessageProperties.EXTERNAL_VIRTUAL_ENDPOINT_IDS);

        assertThat(result).containsExactly("ep-1", "ep-2", "ep-3");
    }

    @Test
    void getDynamicPropertyAsStringList_withMissingKey_returnsNull() {
        var message = new MessageWaitingForAcknowledgement();

        var result = message.getDynamicPropertyAsStringList("non-existent-key");

        assertThat(result).isNull();
    }

    @Test
    void isOlderThanOneWeek_forFreshMessage_returnsFalse() {
        var message = new MessageWaitingForAcknowledgement();

        assertThat(message.isOlderThanOneWeek()).isFalse();
    }

    @Test
    void isOlderThanOneWeek_forMessageOlderThanOneWeek_returnsTrue() {
        var message = new MessageWaitingForAcknowledgement();
        var eightDaysAgo = Instant.now().minusSeconds(60L * 60 * 24 * 8).getEpochSecond();
        ReflectionTestUtils.setField(message, "created", eightDaysAgo);

        assertThat(message.isOlderThanOneWeek()).isTrue();
    }

    @Test
    void isOlderThanOneWeek_forMessageExactlyOneWeekOld_returnsTrue() {
        var message = new MessageWaitingForAcknowledgement();
        var sevenDaysAndOneSecondAgo = Instant.now().minusSeconds(60L * 60 * 24 * 7 + 1).getEpochSecond();
        ReflectionTestUtils.setField(message, "created", sevenDaysAndOneSecondAgo);

        assertThat(message.isOlderThanOneWeek()).isTrue();
    }

    @Test
    void settersAndGetters_workCorrectly() {
        var message = new MessageWaitingForAcknowledgement();
        message.setAgrirouterEndpointId("ar-endpoint-1");
        message.setMessageId("msg-id-1");
        message.setResponse("some error response");
        message.setTechnicalMessageType("iso:11783:-10:time_log:protobuf");

        assertThat(message.getAgrirouterEndpointId()).isEqualTo("ar-endpoint-1");
        assertThat(message.getMessageId()).isEqualTo("msg-id-1");
        assertThat(message.getResponse()).isEqualTo("some error response");
        assertThat(message.getTechnicalMessageType()).isEqualTo("iso:11783:-10:time_log:protobuf");
    }

    @Test
    void dynamicProperties_initiallyEmpty() {
        var message = new MessageWaitingForAcknowledgement();

        assertThat(message.getDynamicProperties()).isEmpty();
    }

    @Test
    void toString_doesNotThrow() {
        var message = new MessageWaitingForAcknowledgement();
        message.setMessageId("msg-001");
        message.setAgrirouterEndpointId("ar-endpoint-001");

        assertThat(message.toString()).contains("msg-001");
    }
}
