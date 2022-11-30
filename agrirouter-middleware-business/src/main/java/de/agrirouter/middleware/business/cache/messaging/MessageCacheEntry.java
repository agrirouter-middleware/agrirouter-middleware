package de.agrirouter.middleware.business.cache.messaging;

import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;

import java.time.Instant;

/**
 * A message cache entry.
 */
public class MessageCacheEntry {
    private String externalEndpointId;
    private PublishNonTelemetryDataParameters publishNonTelemetryDataParameters;
    private long createdAt;
    private long ttl;

    /**
     * @param externalEndpointId                The external endpoint ID.
     * @param publishNonTelemetryDataParameters Parameters for publishing non telemetry data.
     * @param createdAt                         The time when the cache entry was created.
     * @param ttl                               The time to live in seconds, set to 0 if you do not want to have an expiry at all.
     */
    public MessageCacheEntry(
            String externalEndpointId,
            PublishNonTelemetryDataParameters publishNonTelemetryDataParameters,
            long createdAt,
            long ttl
    ) {
        this.externalEndpointId = externalEndpointId;
        this.publishNonTelemetryDataParameters = publishNonTelemetryDataParameters;
        this.createdAt = createdAt;
        this.ttl = ttl;
    }

    /**
     * Check if the cache entry is expired.
     *
     * @return -
     */
    public boolean isExpired() {
        if (ttl == 0) {
            return false;
        }
        long now = Instant.now().getEpochSecond();
        return now - createdAt > ttl;
    }

    public String getExternalEndpointId() {
        return externalEndpointId;
    }

    public void setExternalEndpointId(String externalEndpointId) {
        this.externalEndpointId = externalEndpointId;
    }

    public PublishNonTelemetryDataParameters getPublishNonTelemetryDataParameters() {
        return publishNonTelemetryDataParameters;
    }

    @SuppressWarnings("unused")
    public void setPublishNonTelemetryDataParameters(PublishNonTelemetryDataParameters publishNonTelemetryDataParameters) {
        this.publishNonTelemetryDataParameters = publishNonTelemetryDataParameters;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @SuppressWarnings("unused")
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @SuppressWarnings("unused")
    public long getTtl() {
        return ttl;
    }

    @SuppressWarnings("unused")
    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
}
