package de.agrirouter.middleware.business.cache.messaging;

import de.agrirouter.middleware.business.events.ResendMessageCacheEntryEvent;
import de.agrirouter.middleware.integration.parameters.MessagingIntegrationParameters;
import lombok.extern.slf4j.Slf4j;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Cache for messages.
 */
@Slf4j
@Component
@Scope(value = "singleton")
public class MessageCache {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final EmbeddedStorageManager storageManager;

    @Value("${app.cache.message-cache.batch-size}")
    private int batchSize;

    protected MessageCache(ApplicationEventPublisher applicationEventPublisher, EmbeddedStorageManager storageManager) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.storageManager = storageManager;
        log.trace("###################################################################################################");
        log.trace("Currently cached messages: {}", getCacheRoot().getMessageCache().size());
        getCacheRoot().getMessageCache().forEach(this::trace);
        log.trace("###################################################################################################");
    }

    /**
     * Trace a message within the cache.
     *
     * @param messageCacheEntry A message cache entry.
     */
    private void trace(MessageCacheEntry messageCacheEntry) {
        log.trace("{} : {} | {}", messageCacheEntry.getCreatedAt(), messageCacheEntry.getExternalEndpointId(), messageCacheEntry.getMessagingIntegrationParameters().technicalMessageType());
    }

    /**
     * Send messages within the cache.
     */
    public void sendMessages() {
        Collection<MessageCacheEntry> currentMessageCacheEntries = getOldestCacheEntries();
        log.debug("Re-sending {} messages from cache.", currentMessageCacheEntries.size());
        log.debug("Cleared cache.");
        for (MessageCacheEntry messageCacheEntry : currentMessageCacheEntries) {
            if (getCacheRoot().getMessageCache().remove(messageCacheEntry)) {
                log.debug("Sending message from cache.");
                applicationEventPublisher.publishEvent(new ResendMessageCacheEntryEvent(this, messageCacheEntry.getMessagingIntegrationParameters()));
            } else {
                log.debug("Message cache entry has not been removed from the cache, therefore not sending this one.");
            }
        }
    }

    private Collection<MessageCacheEntry> getOldestCacheEntries() {
        return getCacheRoot().getMessageCache().stream().sorted((o1, o2) -> Long.compare(o2.getCreatedAt(), o1.getCreatedAt())).limit(batchSize).toList();
    }

    /**
     * Count all cache entries
     *
     * @param externalEndpointId The external endpoint ID.
     * @return Number of message cache entries.
     */
    public long countCurrentMessageCacheEntries(String externalEndpointId) {
        return getCurrentMessageCacheEntries().stream()
                .filter(messageCacheEntry -> messageCacheEntry.getExternalEndpointId().equals(externalEndpointId)).count();
    }

    /**
     * Place an entry in the cache.
     *
     * @param externalEndpointId             The external endpoint ID.
     * @param messagingIntegrationParameters Parameters for message sending.
     */
    public void put(String externalEndpointId, MessagingIntegrationParameters messagingIntegrationParameters) {
        log.info("Saving message to cache.");
        log.trace("External endpoint ID: {}", externalEndpointId);
        getCacheRoot().put(externalEndpointId, messagingIntegrationParameters);
        storageManager.storeRoot();
    }

    private CacheRoot getCacheRoot() {
        return (CacheRoot) storageManager.root();
    }

    /**
     * Get all entries.
     *
     * @return All entries from the cache.
     */
    protected Collection<MessageCacheEntry> getCurrentMessageCacheEntries() {
        return getCacheRoot().getMessageCache();
    }

}
