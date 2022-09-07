package de.agrirouter.middleware.business.cache.messaging;

import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Transient cache for messages.
 */
@Slf4j
@Component
@Scope(value = "singleton")
@Profile("!persistent-message-cache")
public class TransientMessageCache extends CommonMessageCache {

    /**
     * The time to live in seconds.
     */
    @Value("${app.cache.transient-message-cache.time-to-live-in-seconds}")
    private int timeToLiveInSeconds;

    /**
     * The message cache.
     */
    private final Map<String, MessageCacheEntry> messageCache = new HashMap<>();

    public TransientMessageCache(ApplicationEventPublisher applicationEventPublisher) {
        super(applicationEventPublisher);
    }

    @Override
    public void put(String externalEndpointId, PublishNonTelemetryDataParameters publishNonTelemetryDataParameters) {
        log.info("Saving message to cache.");
        log.trace("External endpoint ID: {}", externalEndpointId);
        log.trace("Base64 encoded message content: {}", publishNonTelemetryDataParameters.getBase64EncodedMessageContent());
        messageCache.put(externalEndpointId, new MessageCacheEntry(externalEndpointId,
                publishNonTelemetryDataParameters,
                Instant.now().getEpochSecond(),
                timeToLiveInSeconds));
    }

    @Override
    public void clear() {
        log.info("Clearing message cache.");
        messageCache.clear();
    }

    @Override
    protected Collection<MessageCacheEntry> getCurrentMessageCacheEntries() {
        return messageCache.values();
    }
}

