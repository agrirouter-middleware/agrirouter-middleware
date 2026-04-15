package de.agrirouter.middleware.business.cache;

import de.agrirouter.middleware.business.cache.cloud.CloudOnboardingFailureCache;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CloudOnboardingFailureCacheTest {

    private final CloudOnboardingFailureCache cache = new CloudOnboardingFailureCache();

    @Test
    void put_andGet_returnsFailureEntry() {
        var externalEndpointId = "endpoint-123";
        var virtualEndpointId = "virtual-456";

        cache.put(externalEndpointId, virtualEndpointId, "ERR-001", "Connection refused");

        var result = cache.get(virtualEndpointId);
        assertThat(result).isPresent();
        assertThat(result.get().externalEndpointId()).isEqualTo(externalEndpointId);
        assertThat(result.get().virtualExternalEndpointId()).isEqualTo(virtualEndpointId);
        assertThat(result.get().errorCode()).isEqualTo("ERR-001");
        assertThat(result.get().errorMessage()).isEqualTo("Connection refused");
        assertThat(result.get().timestamp()).isNotNull();
    }

    @Test
    void get_withNonExistentVirtualEndpointId_returnsEmpty() {
        var result = cache.get("non-existent-virtual");

        assertThat(result).isEmpty();
    }

    @Test
    void getAll_returnsEntriesForParentEndpoint() {
        var parentEndpointId = "parent-endpoint";
        cache.put(parentEndpointId, "virtual-1", "ERR-001", "Error 1");
        cache.put(parentEndpointId, "virtual-2", "ERR-002", "Error 2");
        cache.put("other-endpoint", "virtual-3", "ERR-003", "Error 3");

        var result = cache.getAll(parentEndpointId);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(e -> e.externalEndpointId().equals(parentEndpointId));
    }

    @Test
    void getAll_withNoEntriesForEndpoint_returnsEmptyList() {
        var result = cache.getAll("no-such-parent");

        assertThat(result).isEmpty();
    }

    @Test
    void clear_removesEntryForVirtualEndpoint() {
        var externalEndpointId = "endpoint-to-clear";
        var virtualEndpointId = "virtual-to-clear";
        cache.put(externalEndpointId, virtualEndpointId, "ERR-999", "Will be cleared");

        cache.clear(virtualEndpointId);

        assertThat(cache.get(virtualEndpointId)).isEmpty();
    }

    @Test
    void clear_onlyRemovesSpecifiedVirtualEndpoint() {
        var parentEndpoint = "endpoint-parent";
        cache.put(parentEndpoint, "virtual-A", "ERR-A", "Error A");
        cache.put(parentEndpoint, "virtual-B", "ERR-B", "Error B");

        cache.clear("virtual-A");

        assertThat(cache.get("virtual-A")).isEmpty();
        assertThat(cache.get("virtual-B")).isPresent();
    }

    @Test
    void put_updatesExistingEntry() {
        var externalEndpointId = "endpoint-update";
        var virtualEndpointId = "virtual-update";
        cache.put(externalEndpointId, virtualEndpointId, "ERR-OLD", "Old message");
        cache.put(externalEndpointId, virtualEndpointId, "ERR-NEW", "New message");

        var result = cache.get(virtualEndpointId);
        assertThat(result).isPresent();
        assertThat(result.get().errorCode()).isEqualTo("ERR-NEW");
        assertThat(result.get().errorMessage()).isEqualTo("New message");
    }
}
