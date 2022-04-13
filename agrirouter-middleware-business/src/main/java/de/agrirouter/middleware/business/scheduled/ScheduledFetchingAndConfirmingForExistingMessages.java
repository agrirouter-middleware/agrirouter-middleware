package de.agrirouter.middleware.business.scheduled;

import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.messaging.mqtt.MessageQueryService;
import com.dke.data.agrirouter.api.service.parameters.MessageQueryParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.MessageQueryServiceImpl;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.EndpointRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service for endpoint maintenance.
 */
@Slf4j
@Service
public class ScheduledFetchingAndConfirmingForExistingMessages {

    private final EndpointRepository endpointRepository;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final MqttClientManagementService mqttClientManagementService;
    private final BusinessOperationLogService businessOperationLogService;

    public ScheduledFetchingAndConfirmingForExistingMessages(EndpointRepository endpointRepository,
                                                             MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                                             MqttClientManagementService mqttClientManagementService,
                                                             BusinessOperationLogService businessOperationLogService) {
        this.endpointRepository = endpointRepository;
        this.mqttClientManagementService = mqttClientManagementService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.businessOperationLogService = businessOperationLogService;
    }

    /**
     * Schedule the fetching and confirming of the existing messages in a cyclic manner.
     */
    @Scheduled(cron = "${app.scheduled.fetching-and-confirming-existing-messages}")
    public void scheduleFetchingAndConfirmingExistingMessagesForAllEndpoints() {
        log.debug("Scheduled fetching and confirming for existing messages.");
        endpointRepository.findAll().stream().filter(endpoint -> !endpoint.isDeactivated()).forEach(endpoint -> {
            this.fetchAndConfirmExistingMessages(endpoint);
            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Scheduled fetching and confirming of existing messages.");
        });
    }

    private void fetchAndConfirmExistingMessages(Endpoint endpoint) {
        log.debug("Fetching and confirming existing messages for endpoint '{}'.", endpoint.getExternalEndpointId());
        final var iMqttClient = mqttClientManagementService.get(endpoint.asOnboardingResponse());
        if (iMqttClient.isEmpty()) {
            log.error(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.asOnboardingResponse().getSensorAlternateId()).asLogMessage());
        } else {
            MessageQueryService messageQueryService = new MessageQueryServiceImpl(iMqttClient.get());
            final var parameters = new MessageQueryParameters();
            parameters.setSentFromInSeconds(Instant.now().minus(28, ChronoUnit.DAYS).getEpochSecond());
            parameters.setSentToInSeconds(Instant.now().getEpochSecond());
            parameters.setOnboardingResponse(endpoint.asOnboardingResponse());
            final var messageId = messageQueryService.send(parameters);

            log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
            MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
            messageWaitingForAcknowledgement.setAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
            messageWaitingForAcknowledgement.setMessageId(messageId);
            messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_FEED_MESSAGE_QUERY.getKey());
            messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
        }
    }

}
