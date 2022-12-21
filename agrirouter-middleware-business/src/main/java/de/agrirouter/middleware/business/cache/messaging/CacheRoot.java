package de.agrirouter.middleware.business.cache.messaging;

import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Cache root object.
 */
public class CacheRoot {
    private List<MessageCacheEntry> messageCache;

    /**
     * Place an entry into the cache.
     *
     * @param externalEndpointId                The external endpoint ID.
     * @param publishNonTelemetryDataParameters Parameters for message sending.
     */
    public void put(String externalEndpointId, PublishNonTelemetryDataParameters publishNonTelemetryDataParameters) {
        if (messageCache == null) {
            messageCache = new ArrayList<>();
        }
        messageCache.add(new MessageCacheEntry(externalEndpointId,
                publishNonTelemetryDataParameters,
                Instant.now().getEpochSecond()));
    }

    /**
     * Get the message cache.
     *
     * @return THe message cache.
     */
    public List<MessageCacheEntry> getMessageCache() {
        if (messageCache == null) {
            messageCache = new ArrayList<>();
        }
        return messageCache;
    }

    /**
     * Setter.
     *
     * @param messageCache The message cache.
     */
    @SuppressWarnings("unused")
    public void setMessageCache(List<MessageCacheEntry> messageCache) {
        this.messageCache = messageCache;
    }
}
