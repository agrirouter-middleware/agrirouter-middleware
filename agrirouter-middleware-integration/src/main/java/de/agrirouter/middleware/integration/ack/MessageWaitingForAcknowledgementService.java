package de.agrirouter.middleware.integration.ack;

import de.agrirouter.middleware.domain.Endpoint;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MessageWaitingForAcknowledgementService {

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
        log.debug("Remove message waiting for ACK, currently there are {} messages waiting for ACK.", messages.size());
        log.trace("{}", messageWaitingForAcknowledgement);
        messages.remove(messageWaitingForAcknowledgement.getMessageId());
        log.debug("Now there are {} messages waiting for ACK.", messages.size());
    }

    /**
     * Saving the message waiting for ACK in the list of messages.
     *
     * @param messageWaitingForAcknowledgement -
     */
    public void save(MessageWaitingForAcknowledgement messageWaitingForAcknowledgement) {
        log.debug("Adding message waiting for ACK, currently there are {} messages waiting for ACK.", messages.size());
        log.trace("{}", messageWaitingForAcknowledgement);
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
        log.info("Clearing all messages waiting for ACK that are older than a week.");
        log.debug("Currently there are {} messages waiting for ACK in total.", messages.size());
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
        log.info("Deleting all messages waiting for ACK for endpoint {}.", endpoint.getAgrirouterEndpointId());
        log.debug("Currently there are {} messages waiting for ACK in total.", messages.size());
        messages
                .values()
                .stream()
                .filter(messageWaitingForAcknowledgement -> messageWaitingForAcknowledgement.getAgrirouterEndpointId().equals(endpoint.getAgrirouterEndpointId()))
                .forEach(this::delete);
    }
}
