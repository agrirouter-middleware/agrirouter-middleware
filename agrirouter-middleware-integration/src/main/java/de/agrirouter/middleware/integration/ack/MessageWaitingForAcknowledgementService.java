package de.agrirouter.middleware.integration.ack;

import de.agrirouter.middleware.domain.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The service for all the messages waiting for ACK.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MessageWaitingForAcknowledgementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageWaitingForAcknowledgementService.class);

    private static final ConcurrentHashMap<String, MessageWaitingForAcknowledgement> messages = new ConcurrentHashMap<>();

    /**
     * Search for the message waiting for ACK.
     *
     * @param messageId -
     * @return -
     */
    public Optional<MessageWaitingForAcknowledgement> findByMessageId(String messageId) {
        final var messageWaitingForAcknowledgement = messages.get(messageId);
        return Optional.ofNullable(messageWaitingForAcknowledgement);
    }

    /**
     * Remove the message waiting for ACK from the list of messages.
     *
     * @param messageWaitingForAcknowledgement -
     */
    public void delete(MessageWaitingForAcknowledgement messageWaitingForAcknowledgement) {
        LOGGER.debug("Remove message waiting for ACK, currently there are {} messages waiting for ACK.", messages.size());
        LOGGER.trace("{}", messageWaitingForAcknowledgement);
        messages.remove(messageWaitingForAcknowledgement.getMessageId());
        LOGGER.debug("Now there are {} messages waiting for ACK.", messages.size());
    }

    /**
     * Saving the message waiting for ACK in the list of messages.
     *
     * @param messageWaitingForAcknowledgement -
     */
    public void save(MessageWaitingForAcknowledgement messageWaitingForAcknowledgement) {
        LOGGER.debug("Adding message waiting for ACK, currently there are {} messages waiting for ACK.", messages.size());
        LOGGER.trace("{}", messageWaitingForAcknowledgement);
        messages.put(messageWaitingForAcknowledgement.getMessageId(), messageWaitingForAcknowledgement);
    }

    /**
     * Find all messages waiting for ACK for a specific endpoint.
     *
     * @param agrirouterEndpointId -
     * @return -
     */
    public List<MessageWaitingForAcknowledgement> findAllForAgrirouterEndpointId(String agrirouterEndpointId) {
        return messages
                .values()
                .stream()
                .filter(messageWaitingForAcknowledgement -> messageWaitingForAcknowledgement.getAgrirouterEndpointId().equals(agrirouterEndpointId))
                .collect(Collectors.toList());
    }

    /**
     * Clear all messages waiting for ACK that are older than a week.
     */
    public void clearAllThatAreOlderThanOneWeek() {
        LOGGER.info("Clearing all messages waiting for ACK that are older than a week.");
        LOGGER.debug("Currently there are {} messages waiting for ACK in total.", messages.size());
        messages
                .values()
                .stream()
                .filter(MessageWaitingForAcknowledgement::isOlderThanOneWeek)
                .forEach(this::delete);
    }

    /**
     * Clear all messages waiting for ACK for a specific endpoint.
     *
     * @param endpoint The endpoint.
     */
    public void deleteAllForEndpoint(Endpoint endpoint) {
        LOGGER.info("Deleting all messages waiting for ACK for endpoint {}.", endpoint.getAgrirouterEndpointId());
        LOGGER.debug("Currently there are {} messages waiting for ACK in total.", messages.size());
        messages
                .values()
                .stream()
                .filter(messageWaitingForAcknowledgement -> messageWaitingForAcknowledgement.getAgrirouterEndpointId().equals(endpoint.getAgrirouterEndpointId()))
                .forEach(this::delete);
    }
}
