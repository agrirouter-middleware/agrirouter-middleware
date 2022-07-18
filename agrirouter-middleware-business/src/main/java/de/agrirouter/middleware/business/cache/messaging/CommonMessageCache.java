package de.agrirouter.middleware.business.cache.messaging;

import de.agrirouter.middleware.business.events.ResendMessageCacheEntryEvent;
import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Collection;

/**
 * Cache for messages.
 */
@Slf4j
abstract class CommonMessageCache implements MessageCache {

    private final ApplicationEventPublisher applicationEventPublisher;

    protected CommonMessageCache(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void sendMessages() {
        Collection<TransientMessageCache.MessageCacheEntry> currentMessageCacheEntries = getCurrentMessageCacheEntries();
        log.debug("Re-sending {} messages from cache.", currentMessageCacheEntries.size());
        clear();
        log.debug("Cleared cache.");
        for (TransientMessageCache.MessageCacheEntry messageCacheEntry : currentMessageCacheEntries) {
            if (messageCacheEntry.isExpired()) {
                log.debug("Message cache entry expired. Skipping.");
            } else {
                log.debug("Sending message from cache.");
                applicationEventPublisher.publishEvent(new ResendMessageCacheEntryEvent(this, messageCacheEntry.publishNonTelemetryDataParameters));
            }
        }
    }

    /**
     * Get all messages in the cache.
     *
     * @return -
     */
    protected abstract Collection<MessageCacheEntry> getCurrentMessageCacheEntries();


    /**
     * Cache entry.
     *
     * @param externalEndpointId                The external endpoint ID.
     * @param publishNonTelemetryDataParameters Parameters for publishing non telemetry data.
     * @param createdAt                         The time when the cache entry was created.
     * @param ttl                               The time to live in seconds.
     */
    public record MessageCacheEntry(
            String externalEndpointId,
            PublishNonTelemetryDataParameters publishNonTelemetryDataParameters,
            long createdAt,
            long ttl
    ) {

        /**
         * Check if the cache entry is expired.
         *
         * @return -
         */
        public boolean isExpired() {
            long now = Instant.now().getEpochSecond();
            return now - createdAt > ttl;
        }

    }
}
