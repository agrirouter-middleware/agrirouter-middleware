package de.agrirouter.middleware.business.cache.messaging;

import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;

/**
 * Cache for messages.
 */
public interface MessageCache {

    /**
     * Save message to the internal cache.
     *
     * @param externalEndpointId                External endpoint ID.
     * @param publishNonTelemetryDataParameters Parameters for publishing non telemetry data.
     */
    void put(String externalEndpointId, PublishNonTelemetryDataParameters publishNonTelemetryDataParameters);

    /**
     * Get message from the internal cache.
     *
     * @param externalEndpointId The external endpoint ID.
     * @return Currently cached messages.
     */
    long countCurrentMessageCacheEntries(String externalEndpointId);

    /**
     * Send all messages in the cache.
     */
    void sendMessages();

    /**
     * Removing all messages.
     */
    void clear();

}
