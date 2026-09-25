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

    @Test
    void getByAgrirouterEndpointId_afterPut_returnsEndpoint() {
        var endpoint = new Endpoint();
        endpoint.setExternalEndpointId("endpoint-123");
        endpoint.setAgrirouterEndpointId("ar-123");

        cache.put(endpoint.getExternalEndpointId(), endpoint);

        var result = cache.getByAgrirouterEndpointId("ar-123");
        assertThat(result).isPresent();
        assertThat(result.get().getExternalEndpointId()).isEqualTo("endpoint-123");
    }

    @Test
    void getByAgrirouterEndpointId_withMissingKey_returnsEmpty() {
        var result = cache.getByAgrirouterEndpointId("ar-unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void remove_alsoDropsTheAgrirouterEndpointIdIndex() {
        var endpoint = new Endpoint();
        endpoint.setExternalEndpointId("endpoint-to-remove");
        endpoint.setAgrirouterEndpointId("ar-to-remove");
        cache.put(endpoint.getExternalEndpointId(), endpoint);

        cache.remove("endpoint-to-remove");

        assertThat(cache.getByAgrirouterEndpointId("ar-to-remove")).isEmpty();
    }

    @Test
    void removeByAgrirouterEndpointId_dropsBothIndices() {
        var endpoint = new Endpoint();
        endpoint.setExternalEndpointId("endpoint-to-remove");
        endpoint.setAgrirouterEndpointId("ar-to-remove");
        cache.put(endpoint.getExternalEndpointId(), endpoint);

        cache.removeByAgrirouterEndpointId("ar-to-remove");

        assertThat(cache.getByAgrirouterEndpointId("ar-to-remove")).isEmpty();
        assertThat(cache.get("endpoint-to-remove")).isEmpty();
    }

    @Test
    void removeByAgrirouterEndpointId_withMissingKey_doesNotThrow() {
        cache.removeByAgrirouterEndpointId("ar-unknown");
        // Just ensure no exception is thrown
    }

    @Test
    void put_withChangedAgrirouterEndpointId_dropsTheFormerIndex() {
        var externalEndpointId = "endpoint-shared";
        var formerEndpoint = new Endpoint();
        formerEndpoint.setExternalEndpointId(externalEndpointId);
        formerEndpoint.setAgrirouterEndpointId("ar-id-1");
        var reonboardedEndpoint = new Endpoint();
        reonboardedEndpoint.setExternalEndpointId(externalEndpointId);
        reonboardedEndpoint.setAgrirouterEndpointId("ar-id-2");

        cache.put(externalEndpointId, formerEndpoint);
        cache.put(externalEndpointId, reonboardedEndpoint);

        assertThat(cache.getByAgrirouterEndpointId("ar-id-1")).isEmpty();
        assertThat(cache.getByAgrirouterEndpointId("ar-id-2")).isPresent();
    }

    @Test
    void put_withoutAgrirouterEndpointId_doesNotIndexTheEndpoint() {
        var externalEndpointId = "endpoint-without-agrirouter-id";
        var endpoint = new Endpoint();
        endpoint.setExternalEndpointId(externalEndpointId);

        cache.put(externalEndpointId, endpoint);

        assertThat(cache.get(externalEndpointId)).isPresent();
    }
}
