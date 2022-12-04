package de.agrirouter.middleware.business.scheduled;

import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Remove messages waiting for acknowledgement which are older than a week.
 */
@Slf4j
@Component
public class ScheduledMessageWaitingForAckRemoval {

    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;

    public ScheduledMessageWaitingForAckRemoval(MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService) {
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
    }

    @Scheduled(cron = "${app.scheduled.message-waiting-for-ack-removal}")
    public void clearAllMessagesWaitingForAck() {
        log.debug("Clearing all messages waiting for ACK.");
        messageWaitingForAcknowledgementService.clearAllThatAreOlderThanOneWeek();
    }
}
