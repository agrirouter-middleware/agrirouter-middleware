package de.agrirouter.middleware.business.cache.messaging;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;

import java.util.List;

/**
 * Cache for messages.
 */
public interface MessageCache {

    /**
     * Save message to the internal cache.
     *
     * @param externalEndpointId          External endpoint ID.
     * @param publishNonTelemetryDataParameters Parameters for publishing non telemetry data.
     */
    void put(String externalEndpointId, PublishNonTelemetryDataParameters publishNonTelemetryDataParameters);

    /**
     * Send all messages in the cache.
     */
    void sendMessages();

    /**
     * Removing all messages.
     */
    void clear();

}
