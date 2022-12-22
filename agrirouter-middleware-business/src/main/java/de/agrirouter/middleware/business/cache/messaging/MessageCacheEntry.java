package de.agrirouter.middleware.business.cache.messaging;

import de.agrirouter.middleware.integration.parameters.MessagingIntegrationParameters;
import lombok.Getter;
import lombok.Setter;

/**
 * A message cache entry.
 */
@Getter
@Setter
public class MessageCacheEntry {
    private String externalEndpointId;
    private MessagingIntegrationParameters messagingIntegrationParameters;
    private long createdAt;

    /**
     * @param externalEndpointId             The external endpoint ID.
     * @param messagingIntegrationParameters Parameters for publishing messages.
     * @param createdAt                      The time when the cache entry was created.
     */
    public MessageCacheEntry(
            String externalEndpointId,
            MessagingIntegrationParameters messagingIntegrationParameters,
            long createdAt
    ) {
        this.externalEndpointId = externalEndpointId;
        this.messagingIntegrationParameters = messagingIntegrationParameters;
        this.createdAt = createdAt;
    }

}
