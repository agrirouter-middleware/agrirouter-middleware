package de.agrirouter.middleware.business.listener;

import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.messaging.mqtt.MessageQueryService;
import com.dke.data.agrirouter.api.service.parameters.MessageQueryParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.MessageQueryServiceImpl;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.SaveAndConfirmExistingMessagesEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.EndpointRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service for endpoint maintenance.
 */
@Slf4j
@Service
public class SaveAndConfirmExistingMessagesEventListener {

    private final EndpointRepository endpointRepository;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final MqttClientManagementService mqttClientManagementService;
    private final BusinessOperationLogService businessOperationLogService;

    public SaveAndConfirmExistingMessagesEventListener(EndpointRepository endpointRepository,
                                                       MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                                       MqttClientManagementService mqttClientManagementService,
                                                       BusinessOperationLogService businessOperationLogService) {
        this.endpointRepository = endpointRepository;
        this.mqttClientManagementService = mqttClientManagementService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.businessOperationLogService = businessOperationLogService;
    }

    /**
     * Handling the event to save and confirm existing messages.
     */
    @EventListener
    public void handleSaveAndConfirmExistingMessagesEvent(SaveAndConfirmExistingMessagesEvent saveAndConfirmExistingMessagesEvent) {
        log.debug("Fetching and confirm existing messages for endpoint '{}'.", saveAndConfirmExistingMessagesEvent.getInternalEndpointId());
        fetchAndConfirmExistingMessages(saveAndConfirmExistingMessagesEvent.getInternalEndpointId());
    }

    /**
     * Fetching and confirming the existing messages for the endpoint.
     *
     * @param externalEndpointId The endpoint.
     */
    private void fetchAndConfirmExistingMessages(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDisabled(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            fetchAndConfirmExistingMessages(endpoint);
            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Fetching and confirming existing messages.");
        } else {
            log.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
        }
    }

    private void fetchAndConfirmExistingMessages(Endpoint endpoint) {
        log.debug("Fetching and confirming existing messages for endpoint '{}'.", endpoint.getExternalEndpointId());
        final var onboardingResponse = endpoint.asOnboardingResponse();
        final var iMqttClient = mqttClientManagementService.get(onboardingResponse);
        if (iMqttClient.isEmpty()) {
            throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(onboardingResponse.getSensorAlternateId()));
        }
        MessageQueryService messageQueryService = new MessageQueryServiceImpl(iMqttClient.get());
        final var parameters = new MessageQueryParameters();
        parameters.setSentFromInSeconds(Instant.now().minus(28, ChronoUnit.DAYS).getEpochSecond());
        parameters.setSentToInSeconds(Instant.now().getEpochSecond());
        parameters.setOnboardingResponse(onboardingResponse);
        final var messageId = messageQueryService.send(parameters);

        log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
        MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
        messageWaitingForAcknowledgement.setAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
        messageWaitingForAcknowledgement.setMessageId(messageId);
        messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_FEED_MESSAGE_QUERY.getKey());
        messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
    }


}
