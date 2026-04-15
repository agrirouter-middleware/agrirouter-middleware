package de.agrirouter.middleware.business.cache;

import de.agrirouter.middleware.business.cache.events.BusinessEvent;
import de.agrirouter.middleware.business.cache.events.BusinessEventApplicationEvent;
import de.agrirouter.middleware.business.cache.events.BusinessEventType;
import de.agrirouter.middleware.business.cache.events.BusinessEventsCache;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessEventsCacheTest {

    private final BusinessEventsCache cache = new BusinessEventsCache();

    @Test
    void handle_newEndpoint_createsNewCacheEntry() {
        var externalEndpointId = "endpoint-new";
        var timestamp = Instant.now();
        var event = new BusinessEvent(timestamp, BusinessEventType.TIME_LOG_RECEIVED);
        var appEvent = new BusinessEventApplicationEvent(this, externalEndpointId, event);

        cache.handle(appEvent);

        var result = cache.get(externalEndpointId);
        assertThat(result).isPresent();
        assertThat(result.get()).containsKey(BusinessEventType.TIME_LOG_RECEIVED);
        assertThat(result.get().get(BusinessEventType.TIME_LOG_RECEIVED)).isEqualTo(timestamp);
    }

    @Test
    void handle_existingEndpoint_updatesExistingCacheEntry() {
        var externalEndpointId = "endpoint-existing";
        var timestamp1 = Instant.now().minusSeconds(60);
        var timestamp2 = Instant.now();

        var event1 = new BusinessEvent(timestamp1, BusinessEventType.DEVICE_DESCRIPTION_RECEIVED);
        cache.handle(new BusinessEventApplicationEvent(this, externalEndpointId, event1));

        var event2 = new BusinessEvent(timestamp2, BusinessEventType.TIME_LOG_RECEIVED);
        cache.handle(new BusinessEventApplicationEvent(this, externalEndpointId, event2));

        var result = cache.get(externalEndpointId);
        assertThat(result).isPresent();
        assertThat(result.get()).containsKey(BusinessEventType.DEVICE_DESCRIPTION_RECEIVED);
        assertThat(result.get()).containsKey(BusinessEventType.TIME_LOG_RECEIVED);
    }

    @Test
    void handle_sameEventType_updatesTimestamp() {
        var externalEndpointId = "endpoint-update";
        var timestamp1 = Instant.now().minusSeconds(120);
        var timestamp2 = Instant.now();

        cache.handle(new BusinessEventApplicationEvent(this, externalEndpointId,
                new BusinessEvent(timestamp1, BusinessEventType.NON_TELEMETRY_MESSAGE_RECEIVED)));
        cache.handle(new BusinessEventApplicationEvent(this, externalEndpointId,
                new BusinessEvent(timestamp2, BusinessEventType.NON_TELEMETRY_MESSAGE_RECEIVED)));

        var result = cache.get(externalEndpointId);
        assertThat(result).isPresent();
        assertThat(result.get().get(BusinessEventType.NON_TELEMETRY_MESSAGE_RECEIVED)).isEqualTo(timestamp2);
    }

    @Test
    void get_withNonExistentEndpoint_returnsEmpty() {
        var result = cache.get("unknown-endpoint");

        assertThat(result).isEmpty();
    }

    @Test
    void handle_multipleEndpoints_storesThemIndependently() {
        var endpoint1 = "endpoint-1";
        var endpoint2 = "endpoint-2";
        var ts1 = Instant.now().minusSeconds(10);
        var ts2 = Instant.now();

        cache.handle(new BusinessEventApplicationEvent(this, endpoint1,
                new BusinessEvent(ts1, BusinessEventType.TASK_DATA_RECEIVED)));
        cache.handle(new BusinessEventApplicationEvent(this, endpoint2,
                new BusinessEvent(ts2, BusinessEventType.TIME_LOG_RECEIVED)));

        assertThat(cache.get(endpoint1).get()).containsKey(BusinessEventType.TASK_DATA_RECEIVED);
        assertThat(cache.get(endpoint1).get()).doesNotContainKey(BusinessEventType.TIME_LOG_RECEIVED);
        assertThat(cache.get(endpoint2).get()).containsKey(BusinessEventType.TIME_LOG_RECEIVED);
        assertThat(cache.get(endpoint2).get()).doesNotContainKey(BusinessEventType.TASK_DATA_RECEIVED);
    }
}
