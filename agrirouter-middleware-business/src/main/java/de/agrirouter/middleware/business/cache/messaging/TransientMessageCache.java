package de.agrirouter.middleware.business.cache.messaging;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import de.agrirouter.middleware.business.PublishNonTelemetryDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transient cache for messages.
 */
@Slf4j
@Component
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

    public TransientMessageCache(PublishNonTelemetryDataService publishNonTelemetryDataService) {
        super(publishNonTelemetryDataService);
    }

    @Override
    public void put(String externalEndpointId, String base64EncodedMessageContent, String filename, List<String> recipients, ContentMessageType contentMessageType) {
        log.info("Saving message to cache.");
        log.trace("External endpoint ID: {}", externalEndpointId);
        log.trace("Base64 encoded message content: {}", base64EncodedMessageContent);
        messageCache.put(externalEndpointId, new MessageCacheEntry(externalEndpointId,
                base64EncodedMessageContent,
                filename,
                recipients,
                contentMessageType,
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

