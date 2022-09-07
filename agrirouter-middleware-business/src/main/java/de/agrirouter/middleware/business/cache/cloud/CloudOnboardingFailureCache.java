package de.agrirouter.middleware.business.cache.cloud;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;

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
        cache.put(externalEndpointId, new FailureEntry(externalEndpointId, virtualExternalEndpointId, errorCode, errorMessage));
    }

    /**
     * The cache entry.
     *
     * @param externalEndpointId        The external endpoint ID.
     * @param virtualExternalEndpointId The virtual external endpoint ID.
     * @param errorCode                 The error code.
     * @param errorMessage              The error message.
     */
    private record FailureEntry(String externalEndpointId, String virtualExternalEndpointId, String errorCode,
                                String errorMessage) {
    }

}
