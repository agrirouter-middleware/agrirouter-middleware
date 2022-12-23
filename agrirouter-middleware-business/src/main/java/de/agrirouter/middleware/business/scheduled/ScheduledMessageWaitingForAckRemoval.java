package de.agrirouter.middleware.business.scheduled;

import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.status.AgrirouterStatusIntegrationService;
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
    private final AgrirouterStatusIntegrationService agrirouterStatusIntegrationService;

    public ScheduledMessageWaitingForAckRemoval(MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                                AgrirouterStatusIntegrationService agrirouterStatusIntegrationService) {
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.agrirouterStatusIntegrationService = agrirouterStatusIntegrationService;
    }

    @Scheduled(cron = "${app.scheduled.message-waiting-for-ack-removal}")
    public void clearAllMessagesWaitingForAck() {
        if (agrirouterStatusIntegrationService.isOperational()) {
            log.debug("Clearing all messages waiting for ACK.");
            messageWaitingForAcknowledgementService.clearAllThatAreOlderThanOneWeek();
        } else {
            log.debug("Clearing all messages waiting for ACK skipped because agrirouter is not operational.");
        }
    }
}
