package de.agrirouter.middleware.business.scheduled;

import de.agrirouter.middleware.business.cache.messaging.MessageCache;
import de.agrirouter.middleware.integration.status.AgrirouterStatusIntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled message sending from cache.
 */
@Slf4j
@Component
public class ScheduledMessageSendingFromCache {

    private final MessageCache messageCache;
    private final AgrirouterStatusIntegrationService agrirouterStatusIntegrationService;

    public ScheduledMessageSendingFromCache(MessageCache messageCache, AgrirouterStatusIntegrationService agrirouterStatusIntegrationService) {
        this.messageCache = messageCache;
        this.agrirouterStatusIntegrationService = agrirouterStatusIntegrationService;
    }

    /**
     * Send messages from cache.
     */
    @Scheduled(cron = "${app.scheduled.empty-message-cache}")
    public void sendMessagesFromCache() {
        if (agrirouterStatusIntegrationService.isOperational()) {
            log.debug("Scheduled message sending from cache.");
            messageCache.sendMessages();
        } else {
            log.debug("Scheduled message sending from cache skipped because agrirouter is not operational.");
        }
    }
}
