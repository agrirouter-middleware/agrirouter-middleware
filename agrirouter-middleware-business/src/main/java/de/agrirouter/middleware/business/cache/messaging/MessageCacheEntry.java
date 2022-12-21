package de.agrirouter.middleware.business.cache.messaging;

import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;
import lombok.Getter;
import lombok.Setter;

/**
 * A message cache entry.
 */
@Getter
@Setter
public class MessageCacheEntry {
    private String externalEndpointId;
    private PublishNonTelemetryDataParameters publishNonTelemetryDataParameters;
    private long createdAt;

    /**
     * @param externalEndpointId                The external endpoint ID.
     * @param publishNonTelemetryDataParameters Parameters for publishing non telemetry data.
     * @param createdAt                         The time when the cache entry was created.
     */
    public MessageCacheEntry(
            String externalEndpointId,
            PublishNonTelemetryDataParameters publishNonTelemetryDataParameters,
            long createdAt
    ) {
        this.externalEndpointId = externalEndpointId;
        this.publishNonTelemetryDataParameters = publishNonTelemetryDataParameters;
        this.createdAt = createdAt;
    }

}
