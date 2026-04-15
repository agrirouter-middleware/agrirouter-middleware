package de.agrirouter.middleware.integration.container;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VirtualEndpointOnboardStateContainerTest {

    private VirtualEndpointOnboardStateContainer container;

    @BeforeEach
    void setUp() {
        container = new VirtualEndpointOnboardStateContainer();
    }

    @Test
    void push_andPop_returnsCorrectState() {
        var messageId = "msg-id-1";
        var externalEndpointId = "external-ep-1";

        container.push(messageId, externalEndpointId);

        var result = container.pop(messageId);
        assertThat(result).isPresent();
        assertThat(result.get().externalEndpointId()).isEqualTo(externalEndpointId);
    }

    @Test
    void pop_withUnknownMessageId_returnsEmpty() {
        var result = container.pop("non-existent-message-id");

        assertThat(result).isEmpty();
    }

    @Test
    void pop_doesNotRemoveEntry() {
        var messageId = "msg-id-pop-twice";
        var externalEndpointId = "ep-pop-twice";
        container.push(messageId, externalEndpointId);

        var first = container.pop(messageId);
        var second = container.pop(messageId);

        assertThat(first).isPresent();
        assertThat(second).isPresent();
    }

    @Test
    void push_overwritesExistingEntryWithSameMessageId() {
        var messageId = "msg-id-overwrite";
        container.push(messageId, "ep-original");
        container.push(messageId, "ep-updated");

        var result = container.pop(messageId);
        assertThat(result).isPresent();
        assertThat(result.get().externalEndpointId()).isEqualTo("ep-updated");
    }

    @Test
    void push_multipleEntries_eachRetrievableByMessageId() {
        container.push("msg-a", "ep-a");
        container.push("msg-b", "ep-b");
        container.push("msg-c", "ep-c");

        assertThat(container.pop("msg-a").get().externalEndpointId()).isEqualTo("ep-a");
        assertThat(container.pop("msg-b").get().externalEndpointId()).isEqualTo("ep-b");
        assertThat(container.pop("msg-c").get().externalEndpointId()).isEqualTo("ep-c");
    }

    @Test
    void onboardState_record_externalEndpointId_isAccessible() {
        var state = new VirtualEndpointOnboardStateContainer.OnboardState("my-external-id");

        assertThat(state.externalEndpointId()).isEqualTo("my-external-id");
    }
}
