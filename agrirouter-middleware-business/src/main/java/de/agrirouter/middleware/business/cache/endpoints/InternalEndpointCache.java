package de.agrirouter.middleware.business.cache.endpoints;

import de.agrirouter.middleware.domain.Endpoint;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal cache to reduce database access.
 * <p>
 * The cache holds a secondary index by the agrirouter© endpoint ID, since the endpoint is looked up by both IDs.
 * Both indices are maintained together, therefore removing an endpoint from the cache clears it from both of them.
 */
@Component
public class InternalEndpointCache {

    private final Map<String, Endpoint> cache = new ConcurrentHashMap<>();
    private final Map<String, Endpoint> cacheByAgrirouterEndpointId = new ConcurrentHashMap<>();


    /**
     * Put an endpoint into the cache.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param endpoint           The endpoint.
     */
    public void put(String externalEndpointId, Endpoint endpoint) {
        final var formerEndpoint = cache.put(externalEndpointId, endpoint);
        if (null != formerEndpoint && null != formerEndpoint.getAgrirouterEndpointId()
                && !formerEndpoint.getAgrirouterEndpointId().equals(endpoint.getAgrirouterEndpointId())) {
            cacheByAgrirouterEndpointId.remove(formerEndpoint.getAgrirouterEndpointId());
        }
        if (null != endpoint.getAgrirouterEndpointId()) {
            cacheByAgrirouterEndpointId.put(endpoint.getAgrirouterEndpointId(), endpoint);
        }
    }

    /**
     * Get the endpoint from the cache.
     */
    public Optional<Endpoint> get(String externalEndpointId) {
        return Optional.ofNullable(cache.get(externalEndpointId));
    }

    /**
     * Get the endpoint from the cache by its agrirouter© endpoint ID.
     *
     * @param agrirouterEndpointId The agrirouter© endpoint ID.
     * @return The endpoint.
     */
    public Optional<Endpoint> getByAgrirouterEndpointId(String agrirouterEndpointId) {
        return Optional.ofNullable(cacheByAgrirouterEndpointId.get(agrirouterEndpointId));
    }

    /**
     * Remove endpoint from cache.
     *
     * @param externalEndpointId -
     */
    public void remove(String externalEndpointId) {
        final var removedEndpoint = cache.remove(externalEndpointId);
        if (null != removedEndpoint && null != removedEndpoint.getAgrirouterEndpointId()) {
            cacheByAgrirouterEndpointId.remove(removedEndpoint.getAgrirouterEndpointId());
        }
    }

    /**
     * Remove endpoint from cache by its agrirouter© endpoint ID.
     *
     * @param agrirouterEndpointId The agrirouter© endpoint ID.
     */
    public void removeByAgrirouterEndpointId(String agrirouterEndpointId) {
        final var removedEndpoint = cacheByAgrirouterEndpointId.remove(agrirouterEndpointId);
        if (null != removedEndpoint && null != removedEndpoint.getExternalEndpointId()) {
            cache.remove(removedEndpoint.getExternalEndpointId());
        }
    }

}
