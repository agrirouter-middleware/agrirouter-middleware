package de.agrirouter.middleware.business.cache.endpoints;

import de.agrirouter.middleware.domain.Endpoint;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Internal cache to reduce database access.
 */
@Component
public class InternalEndpointCache {

    private Map<String, Endpoint> cache = new HashMap<>();


    /**
     * Put an endpoint into the cache.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param endpoint           The endpoint.
     */
    public void put(String externalEndpointId, Endpoint endpoint) {
        cache.put(externalEndpointId, endpoint);
    }

    /**
     * Get the endpoint from the cache.
     */
    public Optional<Endpoint> get(String externalEndpointId) {
        return Optional.ofNullable(cache.get(externalEndpointId));
    }

    /**
     * Remove endpoint from cache.
     *
     * @param externalEndpointId -
     */
    public void remove(String externalEndpointId) {
        cache.remove(externalEndpointId);
    }

}
