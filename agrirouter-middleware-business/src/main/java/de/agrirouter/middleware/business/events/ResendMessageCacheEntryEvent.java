package de.agrirouter.middleware.business.events;

import de.agrirouter.middleware.integration.parameters.MessagingIntegrationParameters;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Will be published if a message from the cache should be resent.
 */
public class ResendMessageCacheEntryEvent extends ApplicationEvent {

    @Getter
    private final MessagingIntegrationParameters messagingIntegrationParameters;

    public ResendMessageCacheEntryEvent(Object source, MessagingIntegrationParameters messagingIntegrationParameters) {
        super(source);
        this.messagingIntegrationParameters = messagingIntegrationParameters;
    }
}
