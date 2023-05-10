package de.agrirouter.middleware.integration.mqtt.list_endpoints.cache;

import de.agrirouter.middleware.integration.mqtt.list_endpoints.MessageRecipient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

/**
 * Cache for the message recipients. Will be used if there is no connection to the agrirouter® or
 * there is no answer from the agrirouter®.
 */
@Slf4j
@Component
public class MessageRecipientCache {

    private final HashMap<String, MessageRecipientCacheEntry> cache = new HashMap<>();

    /**
     * Place a collection of message recipients into the cache.
     */
    public void put(String externalEndpointId, Collection<MessageRecipient> messageRecipients) {
        log.debug("Placing message recipients into cache for external endpoint ID {}.", externalEndpointId);
        messageRecipients.stream()
                .map(MessageRecipient::deepCopy)
                .forEach(messageRecipient -> messageRecipient.setCached(true));
        this.cache.put(externalEndpointId, MessageRecipientCacheEntry.builder()
                .messageRecipients(messageRecipients)
                .timestamp(Instant.now())
                .build());
    }

    /**
     * Get the optional message recipients from the cache.
     */
    public Optional<Collection<MessageRecipient>> get(String externalEndpointId) {
        var messageRecipientCacheEntry = this.cache.get(externalEndpointId);
        if (messageRecipientCacheEntry != null) {
            log.debug("Message recipients found in cache for external endpoint ID {}.", externalEndpointId);
            log.debug("Message recipients have been placed in cache at {}.", messageRecipientCacheEntry.getTimestamp());
            return Optional.ofNullable(messageRecipientCacheEntry.getMessageRecipients());
        }
        log.debug("No message recipients found in cache for external endpoint ID {}.", externalEndpointId);
        return Optional.empty();
    }

}
