package de.agrirouter.middleware.integration.mqtt.list_endpoints.cache;

import de.agrirouter.middleware.integration.mqtt.list_endpoints.MessageRecipient;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Collection;

/**
 * Cache entry for the message recipients.
 */
@Getter
@Builder
public class MessageRecipientCacheEntry {

    /**
     * The timestamp when the message recipients have been fetched and placed in the cache.
     */
    private final Instant timestamp;

    /**
     * The recipients of the connected endpoint.
     */
    private final Collection<MessageRecipient> messageRecipients;

}
