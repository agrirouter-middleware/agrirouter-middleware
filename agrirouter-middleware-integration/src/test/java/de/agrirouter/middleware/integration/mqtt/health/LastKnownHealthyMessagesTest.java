package de.agrirouter.middleware.integration.mqtt.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class LastKnownHealthyMessagesTest {

    private LastKnownHealthyMessages lastKnownHealthyMessages;

    @BeforeEach
    void setUp() {
        lastKnownHealthyMessages = new LastKnownHealthyMessages();
    }

    @Test
    void put_andGet_returnsRecentInstant() {
        var before = Instant.now();

        lastKnownHealthyMessages.put("ar-ep-1");

        var after = Instant.now();
        var result = lastKnownHealthyMessages.get("ar-ep-1");

        assertThat(result).isPresent();
        assertThat(result.get()).isBetween(before, after);
    }

    @Test
    void get_withUnknownEndpointId_returnsEmpty() {
        var result = lastKnownHealthyMessages.get("non-existent-ep");

        assertThat(result).isEmpty();
    }

    @Test
    void put_twice_updatesTimestamp() {
        lastKnownHealthyMessages.put("ar-ep-update");
        var first = lastKnownHealthyMessages.get("ar-ep-update").orElseThrow();
        lastKnownHealthyMessages.put("ar-ep-update");
        var second = lastKnownHealthyMessages.get("ar-ep-update").orElseThrow();

        assertThat(second).isAfterOrEqualTo(first);
    }

    @Test
    void put_multipleEndpoints_eachRetrievableIndependently() {
        var before = Instant.now();

        lastKnownHealthyMessages.put("ar-ep-A");
        lastKnownHealthyMessages.put("ar-ep-B");

        assertThat(lastKnownHealthyMessages.get("ar-ep-A")).isPresent();
        assertThat(lastKnownHealthyMessages.get("ar-ep-B")).isPresent();
        assertThat(lastKnownHealthyMessages.get("ar-ep-C")).isEmpty();
    }
}
