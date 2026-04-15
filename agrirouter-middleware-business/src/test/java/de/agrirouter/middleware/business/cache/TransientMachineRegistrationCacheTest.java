package de.agrirouter.middleware.business.cache;

import de.agrirouter.middleware.business.cache.registration.TransientMachineRegistrationCache;
import de.agrirouter.middleware.business.parameters.RegisterMachineParameters;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class TransientMachineRegistrationCacheTest {

    private final TransientMachineRegistrationCache cache = new TransientMachineRegistrationCache();

    @Test
    void put_andPop_returnsCacheEntry() {
        ReflectionTestUtils.setField(cache, "timeToLiveInSeconds", 60);
        var externalEndpointId = "endpoint-123";
        var teamSetContextId = "team-set-1";
        var params = createParams(externalEndpointId, "base64-device-desc");

        cache.put(externalEndpointId, teamSetContextId, params);
        var result = cache.pop(externalEndpointId);

        assertThat(result).isPresent();
        assertThat(result.get().externalEndpointId()).isEqualTo(externalEndpointId);
        assertThat(result.get().teamSetContextId()).isEqualTo(teamSetContextId);
        assertThat(result.get().registerMachineParameters()).isEqualTo(params);
    }

    @Test
    void pop_withNonExistentKey_returnsEmpty() {
        var result = cache.pop("no-such-endpoint");

        assertThat(result).isEmpty();
    }

    @Test
    void pop_removesEntryFromCache() {
        ReflectionTestUtils.setField(cache, "timeToLiveInSeconds", 60);
        var externalEndpointId = "endpoint-remove";
        cache.put(externalEndpointId, "team-set", createParams(externalEndpointId, "encoded"));

        cache.pop(externalEndpointId);

        assertThat(cache.pop(externalEndpointId)).isEmpty();
    }

    @Test
    void pop_withExpiredEntry_returnsEmpty() {
        // Use a negative TTL to make entry immediately expired
        ReflectionTestUtils.setField(cache, "timeToLiveInSeconds", -1);
        var externalEndpointId = "endpoint-expired";
        cache.put(externalEndpointId, "team-set", createParams(externalEndpointId, "encoded"));

        var result = cache.pop(externalEndpointId);
        assertThat(result).isEmpty();
    }

    @Test
    void isExpired_withFreshEntry_returnsFalse() {
        var entry = new TransientMachineRegistrationCache.MachineRegistrationCacheEntry(
                "endpoint-fresh", "team-set", createParams("endpoint-fresh", "encoded"),
                System.currentTimeMillis() / 1000L, 60L);

        assertThat(entry.isExpired()).isFalse();
    }

    @Test
    void isExpired_withExpiredEntry_returnsTrue() {
        // Created 120 seconds ago with TTL of 60 seconds
        var createdAt = (System.currentTimeMillis() / 1000L) - 120L;
        var entry = new TransientMachineRegistrationCache.MachineRegistrationCacheEntry(
                "endpoint-expired", "team-set", createParams("endpoint-expired", "encoded"),
                createdAt, 60L);

        assertThat(entry.isExpired()).isTrue();
    }

    @Test
    void isExpired_withTtlExactlyMet_returnsTrue() {
        // Created exactly TTL seconds ago
        long ttl = 30L;
        var createdAt = (System.currentTimeMillis() / 1000L) - ttl - 1;
        var entry = new TransientMachineRegistrationCache.MachineRegistrationCacheEntry(
                "endpoint-met", "team-set", createParams("endpoint-met", "encoded"),
                createdAt, ttl);

        assertThat(entry.isExpired()).isTrue();
    }

    private RegisterMachineParameters createParams(String externalEndpointId, String base64Desc) {
        var params = new RegisterMachineParameters();
        params.setExternalEndpointId(externalEndpointId);
        params.setBase64EncodedDeviceDescription(base64Desc);
        return params;
    }
}
