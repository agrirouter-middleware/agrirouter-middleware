package de.agrirouter.middleware.business.cache.messaging;

import de.agrirouter.middleware.integration.parameters.MessagingIntegrationParameters;

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
     * @param externalEndpointId             The external endpoint ID.
     * @param messagingIntegrationParameters Parameters for message sending.
     */
    public void put(String externalEndpointId, MessagingIntegrationParameters messagingIntegrationParameters) {
        if (messageCache == null) {
            messageCache = new ArrayList<>();
        }
        messageCache.add(new MessageCacheEntry(externalEndpointId,
                messagingIntegrationParameters,
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
