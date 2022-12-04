package de.agrirouter.middleware.business.cache.messaging;

import de.agrirouter.middleware.business.events.ResendMessageCacheEntryEvent;
import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;
import lombok.extern.slf4j.Slf4j;
import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
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

    @Value("${app.cache.message-cache.time-to-live-in-seconds}")
    private long timeToLiveInSeconds;

    @Value("${app.cache.message-cache.batch-size}")
    private int batchSize;

    protected MessageCache(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.storageManager = embeddedStorageManager();
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
        log.trace("{} : {} | {}", messageCacheEntry.getCreatedAt(), messageCacheEntry.getExternalEndpointId(), messageCacheEntry.getPublishNonTelemetryDataParameters().getContentMessageType());
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
                if (messageCacheEntry.isExpired()) {
                    log.debug("Message cache entry expired. Skipped sending, just removing this one from the cache.");
                } else {
                    log.debug("Sending message from cache.");
                    applicationEventPublisher.publishEvent(new ResendMessageCacheEntryEvent(this, messageCacheEntry.getPublishNonTelemetryDataParameters()));
                }
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
     * @param externalEndpointId                The external endpoint ID.
     * @param publishNonTelemetryDataParameters Parameters for message sending.
     */
    public void put(String externalEndpointId, PublishNonTelemetryDataParameters publishNonTelemetryDataParameters) {
        log.info("Saving message to cache.");
        log.trace("External endpoint ID: {}", externalEndpointId);
        log.trace("Base64 encoded message content: {}", publishNonTelemetryDataParameters.getBase64EncodedMessageContent());
        getCacheRoot().put(externalEndpointId, publishNonTelemetryDataParameters, timeToLiveInSeconds);
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

    private EmbeddedStorageManager embeddedStorageManager() {
        CacheRoot cacheRoot = new CacheRoot();
        return EmbeddedStorage.Foundation(Paths.get(System.getProperty("user.home"), ".agrirouter-middleware", "message-cache"))
                .onConnectionFoundation(cf -> cf.setClassLoaderProvider(ClassLoaderProvider.New(
                        Thread.currentThread().getContextClassLoader()))).start(cacheRoot);
    }

}
