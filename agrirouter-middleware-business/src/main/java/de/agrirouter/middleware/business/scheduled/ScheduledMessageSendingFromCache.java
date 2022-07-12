package de.agrirouter.middleware.business.scheduled;

import de.agrirouter.middleware.business.cache.messaging.MessageCache;
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

    public ScheduledMessageSendingFromCache(MessageCache messageCache) {
        this.messageCache = messageCache;
    }

    /**
     * Send messages from cache.
     */
    @Scheduled(cron = "${app.scheduled.empty-message-cache}")
    public void sendMessagesFromCache() {
        messageCache.sendMessages();
    }
}
