package de.agrirouter.middleware.business.cache.messaging;

import de.agrirouter.middleware.business.events.ResendMessageCacheEntryEvent;
import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;
import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.Collection;

/**
 * Cache for messages.
 */
@Component
@Scope(value = "singleton")
public class MessageCache {

    private static final Logger LOG = LoggerFactory.getLogger(MessageCache.class);

    private final ApplicationEventPublisher applicationEventPublisher;
    private final EmbeddedStorageManager storageManager;

    @Value("${app.cache.message-cache.time-to-live-in-seconds}")
    private long timeToLiveInSeconds;

    protected MessageCache(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.storageManager = embeddedStorageManager();
        LOG.trace("###################################################################################################");
        LOG.trace("Currently cached messages: {}", getCacheRoot().getMessageCache().size());
        getCacheRoot().getMessageCache().forEach(this::trace);
        LOG.trace("###################################################################################################");
    }

    /**
     * Trace a message within the cache.
     *
     * @param messageCacheEntry A message cache entry.
     */
    private void trace(MessageCacheEntry messageCacheEntry) {
        LOG.trace("{} : {} | {}", messageCacheEntry.getCreatedAt(), messageCacheEntry.getExternalEndpointId(), messageCacheEntry.getPublishNonTelemetryDataParameters().getContentMessageType());
    }

    /**
     * Send messages within the cache.
     */
    public void sendMessages() {
        Collection<MessageCacheEntry> currentMessageCacheEntries = getCurrentMessageCacheEntries();
        LOG.debug("Re-sending {} messages from cache.", currentMessageCacheEntries.size());
        clear();
        LOG.debug("Cleared cache.");
        for (MessageCacheEntry messageCacheEntry : currentMessageCacheEntries) {
            if (messageCacheEntry.isExpired()) {
                LOG.debug("Message cache entry expired. Skipping.");
            } else {
                LOG.debug("Sending message from cache.");
                applicationEventPublisher.publishEvent(new ResendMessageCacheEntryEvent(this, messageCacheEntry.getPublishNonTelemetryDataParameters()));
            }
        }
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
        LOG.info("Saving message to cache.");
        LOG.trace("External endpoint ID: {}", externalEndpointId);
        LOG.trace("Base64 encoded message content: {}", publishNonTelemetryDataParameters.getBase64EncodedMessageContent());
        getCacheRoot().put(externalEndpointId, publishNonTelemetryDataParameters, timeToLiveInSeconds);
        storageManager.storeRoot();
    }

    private CacheRoot getCacheRoot() {
        return (CacheRoot) storageManager.root();
    }

    /**
     * Clear the cache.
     */
    public void clear() {
        LOG.info("Clearing message cache.");
        getCacheRoot().getMessageCache().clear();
        storageManager.storeRoot();
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
