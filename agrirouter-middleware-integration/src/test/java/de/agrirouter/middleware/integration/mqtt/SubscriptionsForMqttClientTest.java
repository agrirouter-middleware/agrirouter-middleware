package de.agrirouter.middleware.integration.mqtt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionsForMqttClientTest {

    private SubscriptionsForMqttClient subscriptions;

    @BeforeEach
    void setUp() {
        subscriptions = new SubscriptionsForMqttClient();
    }

    @Test
    void add_andExists_returnsTrue() {
        subscriptions.add("client-1", "topic/test");

        assertThat(subscriptions.exists("client-1", "topic/test")).isTrue();
    }

    @Test
    void exists_forUnknownClient_returnsFalse() {
        assertThat(subscriptions.exists("non-existent-client", "topic/test")).isFalse();
    }

    @Test
    void exists_forUnknownTopic_returnsFalse() {
        subscriptions.add("client-1", "topic/known");

        assertThat(subscriptions.exists("client-1", "topic/unknown")).isFalse();
    }

    @Test
    void add_sameTopicTwice_doesNotDuplicate() {
        subscriptions.add("client-1", "topic/dupe");
        subscriptions.add("client-1", "topic/dupe");

        assertThat(subscriptions.getSubscriptions().get("client-1")).hasSize(1);
    }

    @Test
    void add_multipleTopicsForSameClient_allExist() {
        subscriptions.add("client-1", "topic/a");
        subscriptions.add("client-1", "topic/b");
        subscriptions.add("client-1", "topic/c");

        assertThat(subscriptions.exists("client-1", "topic/a")).isTrue();
        assertThat(subscriptions.exists("client-1", "topic/b")).isTrue();
        assertThat(subscriptions.exists("client-1", "topic/c")).isTrue();
    }

    @Test
    void add_topicsForDifferentClients_areIsolated() {
        subscriptions.add("client-A", "topic/shared");
        subscriptions.add("client-B", "topic/shared");

        assertThat(subscriptions.exists("client-A", "topic/shared")).isTrue();
        assertThat(subscriptions.exists("client-B", "topic/shared")).isTrue();
        assertThat(subscriptions.getSubscriptions()).hasSize(2);
    }

    @Test
    void clear_removesAllTopicsForClient() {
        subscriptions.add("client-to-clear", "topic/x");
        subscriptions.add("client-to-clear", "topic/y");

        subscriptions.clear("client-to-clear");

        assertThat(subscriptions.exists("client-to-clear", "topic/x")).isFalse();
        assertThat(subscriptions.exists("client-to-clear", "topic/y")).isFalse();
    }

    @Test
    void clear_doesNotAffectOtherClients() {
        subscriptions.add("client-keep", "topic/keep");
        subscriptions.add("client-remove", "topic/remove");

        subscriptions.clear("client-remove");

        assertThat(subscriptions.exists("client-keep", "topic/keep")).isTrue();
        assertThat(subscriptions.exists("client-remove", "topic/remove")).isFalse();
    }

    @Test
    void clear_nonExistentClient_doesNotThrow() {
        subscriptions.clear("no-such-client");
        // Should not throw
    }

    @Test
    void getSubscriptions_initiallyEmpty() {
        assertThat(subscriptions.getSubscriptions()).isEmpty();
    }
}
