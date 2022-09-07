package de.agrirouter.middleware.business.cache.cloud;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

/**
 * Cache for cloud onboarding failures.
 */
@Component
@Scope(value = "singleton")
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
    public List<FailureEntry> get(String externalEndpointId) {
        return cache.values().stream().filter(failureEntry -> failureEntry.externalEndpointId.equals(externalEndpointId)).toList();
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
