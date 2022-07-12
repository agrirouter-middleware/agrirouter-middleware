package de.agrirouter.middleware.business.cache.messaging;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import de.agrirouter.middleware.business.PublishNonTelemetryDataService;
import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * Cache for messages.
 */
@Slf4j
abstract class CommonMessageCache implements MessageCache {

    private final PublishNonTelemetryDataService publishNonTelemetryDataService;

    protected CommonMessageCache(PublishNonTelemetryDataService publishNonTelemetryDataService) {
        this.publishNonTelemetryDataService = publishNonTelemetryDataService;
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
                PublishNonTelemetryDataParameters publishNonTelemetryDataParameters = new PublishNonTelemetryDataParameters(
                        messageCacheEntry.externalEndpointId,
                        messageCacheEntry.base64EncodedMessageContent,
                        messageCacheEntry.contentMessageType,
                        messageCacheEntry.filename,
                        messageCacheEntry.recipients
                );
                publishNonTelemetryDataService.publish(publishNonTelemetryDataParameters);
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
     * @param externalEndpointId          The external endpoint ID.
     * @param base64EncodedMessageContent The base64 encoded message content.
     * @param filename                    The filename.
     * @param recipients                  The recipients.
     * @param contentMessageType          The content message type.
     * @param createdAt                   The time when the cache entry was created.
     * @param ttl                         The time to live in seconds.
     */
    public record MessageCacheEntry(
            String externalEndpointId,
            String base64EncodedMessageContent,
            String filename,
            List<String> recipients,
            ContentMessageType contentMessageType,
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
