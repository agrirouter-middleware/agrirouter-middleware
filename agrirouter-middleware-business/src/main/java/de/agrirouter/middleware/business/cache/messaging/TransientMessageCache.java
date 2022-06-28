package de.agrirouter.middleware.business.cache.messaging;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import de.agrirouter.middleware.business.PublishNonTelemetryDataService;
import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache for messages.
 */
@Slf4j
@Component
@Profile("!persistent-message-cache")
public class TransientMessageCache implements MessageCache {

    private final Map<String, MessageCacheEntry> messageCache = new HashMap<>();

    private final PublishNonTelemetryDataService publishNonTelemetryDataService;

    public TransientMessageCache(PublishNonTelemetryDataService publishNonTelemetryDataService) {
        this.publishNonTelemetryDataService = publishNonTelemetryDataService;
    }

    /**
     * Save message to the internal cache.
     *
     * @param externalEndpointId          -
     * @param base64EncodedMessageContent -
     * @param filename                    -
     * @param recipients                  -
     * @param contentMessageType          -
     */
    @Override
    public void put(String externalEndpointId, String base64EncodedMessageContent, String filename, List<String> recipients, ContentMessageType contentMessageType) {
        log.info("Saving message to cache.");
        log.trace("External endpoint ID: {}", externalEndpointId);
        log.trace("Base64 encoded message content: {}", base64EncodedMessageContent);
        messageCache.put(externalEndpointId, new MessageCacheEntry(externalEndpointId,
                base64EncodedMessageContent,
                filename,
                recipients,
                contentMessageType));
    }

    @Override
    public void sendMessages() {
        Collection<MessageCacheEntry> currentMessageCacheEntries = messageCache.values();
        for (MessageCacheEntry messageCacheEntry : currentMessageCacheEntries) {
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

    /**
     * Cache entry.
     *
     * @param externalEndpointId          The external endpoint ID.
     * @param base64EncodedMessageContent The base64 encoded message content.
     * @param filename                    The filename.
     * @param recipients                  The recipients.
     * @param contentMessageType          The content message type.
     */
    public record MessageCacheEntry(
            String externalEndpointId,
            String base64EncodedMessageContent,
            String filename,
            List<String> recipients,
            ContentMessageType contentMessageType
    ) {
    }
}

