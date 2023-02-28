package de.agrirouter.middleware.business.cache.cloud;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Cache for cloud onboarding failures.
 */
@Component
public class CloudOnboardingFailureCache {

    private final HashMap<String, FailureEntry> cache = new HashMap<>();

    /**
     * Add a new entry to the cache.
     *
     * @param externalEndpointId        The external endpoint ID.
     * @param virtualExternalEndpointId The virtual external endpoint ID.
     * @param errorCode                 The error code.
     * @param errorMessage              The error message.
     */
    public void put(String externalEndpointId, String virtualExternalEndpointId, String errorCode, String errorMessage) {
        cache.put(virtualExternalEndpointId, new FailureEntry(Instant.now(), externalEndpointId, virtualExternalEndpointId, errorCode, errorMessage));
    }

    /**
     * Get all entries for the external endpoint ID.
     */
    public List<FailureEntry> getAll(String externalEndpointId) {
        return cache.values().stream().filter(failureEntry -> failureEntry.externalEndpointId.equals(externalEndpointId)).toList();
    }

    /**
     * Get the entry for the virtual external endpoint ID.
     */
    public Optional<FailureEntry> get(String virtualExternalEndpointId) {
        return Optional.ofNullable(cache.get(virtualExternalEndpointId));
    }

    /**
     * Clear all failures for the external virtual endpoint ID.
     *
     * @param externalVirtualEndpointId The external virtual endpoint ID.
     */
    public void clear(String externalVirtualEndpointId) {
        cache.remove(externalVirtualEndpointId);
    }

    /**
     * The cache entry.
     *
     * @param timestamp                 The timestamp.
     * @param externalEndpointId        The external endpoint ID.
     * @param virtualExternalEndpointId The virtual external endpoint ID.
     * @param errorCode                 The error code.
     * @param errorMessage              The error message.
     */
    public record FailureEntry(Instant timestamp, String externalEndpointId, String virtualExternalEndpointId,
                               String errorCode,
                               String errorMessage) {
    }

}
