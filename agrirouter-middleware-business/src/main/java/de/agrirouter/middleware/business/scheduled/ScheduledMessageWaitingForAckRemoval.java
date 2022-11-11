package de.agrirouter.middleware.business.scheduled;

import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledMessageWaitingForAckRemoval {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledMessageWaitingForAckRemoval.class);

    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;

    public ScheduledMessageWaitingForAckRemoval(MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService) {
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
    }

    @Scheduled(cron = "${app.scheduled.message-waiting-for-ack-removal}")
    public void clearAllMessagesWaitingForAck() {
        LOGGER.debug("Clearing all messages waiting for ACK.");
        messageWaitingForAcknowledgementService.clearAllThatAreOlderThanOneWeek();
    }
}
