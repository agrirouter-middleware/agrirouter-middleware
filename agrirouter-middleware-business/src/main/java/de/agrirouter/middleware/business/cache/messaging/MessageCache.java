package de.agrirouter.middleware.business.cache.messaging;

import de.agrirouter.middleware.business.events.ResendMessageCacheEntryEvent;
import de.agrirouter.middleware.domain.documents.MessageCacheEntry;
import de.agrirouter.middleware.integration.parameters.MessagingIntegrationParameters;
import de.agrirouter.middleware.persistence.mongo.MessageCacheEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ThreadUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;

/**
 * Cache for messages.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageCache {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessageCacheEntryRepository messageCacheEntryRepository;

    @Value("${app.cache.message-cache.batch-size}")
    private int batchSize;

    @Value("${app.cache.message-cache.batch-sleep-time-seconds}")
    private int sleepTimeSeconds;

    @PostConstruct
    public void init() {
        log.trace("###################################################################################################");
        log.trace("Currently cached messages: {}", messageCacheEntryRepository.count());
        log.trace("###################################################################################################");
    }

    /**
     * Trace a message within the cache.
     *
     * @param messageCacheEntry A message cache entry.
     */
    private void trace(MessageCacheEntry messageCacheEntry) {
        log.trace("{} : {} | {}", messageCacheEntry.getCreatedAt(), messageCacheEntry.getExternalEndpointId(), messageCacheEntry.getTechnicalMessageType());
    }

    /**
     * Send messages within the cache.
     */
    public void sendMessages() {
        var page = messageCacheEntryRepository.findAll(PageRequest.of(0, batchSize, Sort.by(Sort.Order.desc("createdAt"))));
        while (page.hasContent()) {
            log.debug("There are {} messages in this batch.", page.getContent().size());
            for (var messageCacheEntry : page.getContent()) {
                trace(messageCacheEntry);
                messageCacheEntryRepository.delete(messageCacheEntry);
                applicationEventPublisher.publishEvent(new ResendMessageCacheEntryEvent(this, new MessagingIntegrationParameters(messageCacheEntry.getExternalEndpointId(),
                        messageCacheEntry.getTechnicalMessageType(),
                        messageCacheEntry.getRecipients(),
                        messageCacheEntry.getFilename(),
                        messageCacheEntry.getMessage(),
                        messageCacheEntry.getTeamSetContextId())));
            }
            if (page.hasNext()) {
                log.debug("Still messages to send, therefore sending the remaining messages after a short break.");
                try {
                    ThreadUtils.sleep(Duration.ofSeconds(sleepTimeSeconds));
                    log.debug("Sending remaining messages.");
                    page = messageCacheEntryRepository.findAll(page.nextPageable());
                } catch (InterruptedException e) {
                    log.error("There was an error while waiting for the next batch of messages to be sent.", e);
                }
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
        return messageCacheEntryRepository.countAllByExternalEndpointId(externalEndpointId);
    }

    /**
     * Place an entry in the cache.
     *
     * @param externalEndpointId             The external endpoint ID.
     * @param messagingIntegrationParameters Parameters for message sending.
     */
    public void put(String externalEndpointId, MessagingIntegrationParameters messagingIntegrationParameters) {
        log.info("Saving message to cache.");
        log.trace("External endpoint ID: {}", externalEndpointId);
        var messageCacheEntry = new MessageCacheEntry(
                externalEndpointId,
                messagingIntegrationParameters.technicalMessageType(),
                messagingIntegrationParameters.recipients(),
                messagingIntegrationParameters.filename(),
                messagingIntegrationParameters.message(),
                messagingIntegrationParameters.teamSetContextId(),
                Instant.now(),
                Instant.now().plus(Duration.ofDays(14))
        );
        messageCacheEntryRepository.save(messageCacheEntry);
    }

}
