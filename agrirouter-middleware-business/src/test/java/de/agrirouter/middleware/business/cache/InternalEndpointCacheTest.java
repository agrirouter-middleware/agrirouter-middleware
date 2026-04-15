package de.agrirouter.middleware.business.cache;

import de.agrirouter.middleware.business.cache.endpoints.InternalEndpointCache;
import de.agrirouter.middleware.domain.Endpoint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InternalEndpointCacheTest {

    private final InternalEndpointCache cache = new InternalEndpointCache();

    @Test
    void put_andGet_returnsEndpoint() {
        var externalEndpointId = "endpoint-123";
        var endpoint = new Endpoint();
        endpoint.setExternalEndpointId(externalEndpointId);

        cache.put(externalEndpointId, endpoint);

        var result = cache.get(externalEndpointId);
        assertThat(result).isPresent();
        assertThat(result.get().getExternalEndpointId()).isEqualTo(externalEndpointId);
    }

    @Test
    void get_withMissingKey_returnsEmpty() {
        var result = cache.get("non-existent-endpoint");

        assertThat(result).isEmpty();
    }

    @Test
    void remove_removesEndpointFromCache() {
        var externalEndpointId = "endpoint-to-remove";
        var endpoint = new Endpoint();
        cache.put(externalEndpointId, endpoint);

        cache.remove(externalEndpointId);

        assertThat(cache.get(externalEndpointId)).isEmpty();
    }

    @Test
    void remove_nonExistentKey_doesNotThrow() {
        cache.remove("no-such-endpoint");
        // Just ensure no exception is thrown
    }

    @Test
    void put_overwritesExistingEntry() {
        var externalEndpointId = "endpoint-shared";
        var endpoint1 = new Endpoint();
        endpoint1.setAgrirouterEndpointId("ar-id-1");
        var endpoint2 = new Endpoint();
        endpoint2.setAgrirouterEndpointId("ar-id-2");

        cache.put(externalEndpointId, endpoint1);
        cache.put(externalEndpointId, endpoint2);

        var result = cache.get(externalEndpointId);
        assertThat(result).isPresent();
        assertThat(result.get().getAgrirouterEndpointId()).isEqualTo("ar-id-2");
    }

    @Test
    void put_multipleEntries_retrievesEachCorrectly() {
        var id1 = "endpoint-A";
        var id2 = "endpoint-B";
        var ep1 = new Endpoint();
        ep1.setAgrirouterEndpointId("ar-A");
        var ep2 = new Endpoint();
        ep2.setAgrirouterEndpointId("ar-B");

        cache.put(id1, ep1);
        cache.put(id2, ep2);

        assertThat(cache.get(id1).get().getAgrirouterEndpointId()).isEqualTo("ar-A");
        assertThat(cache.get(id2).get().getAgrirouterEndpointId()).isEqualTo("ar-B");
    }
}
