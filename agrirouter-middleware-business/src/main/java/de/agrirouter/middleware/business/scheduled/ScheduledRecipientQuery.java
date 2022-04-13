package de.agrirouter.middleware.business.scheduled;

import agrirouter.request.payload.account.Endpoints;
import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.parameters.ListEndpointsParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.ListEndpointsServiceImpl;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.EndpointRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled fetching of possible recipients.
 */
@Slf4j
@Component
public class ScheduledRecipientQuery {

    private final EndpointRepository endpointRepository;
    private final MqttClientManagementService mqttClientManagementService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final BusinessOperationLogService businessOperationLogService;

    public ScheduledRecipientQuery(EndpointRepository endpointRepository,
                                   MqttClientManagementService mqttClientManagementService,
                                   MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                   BusinessOperationLogService businessOperationLogService) {
        this.endpointRepository = endpointRepository;
        this.mqttClientManagementService = mqttClientManagementService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.businessOperationLogService = businessOperationLogService;
    }

    /**
     * Schedule the recipient checking in a cyclic manner.
     */
    @Scheduled(cron = "${app.scheduled.recipient-query}")
    public void scheduledStatusLogging() {
        log.debug("Scheduled recipient checking for all endpoints.");
        endpointRepository.findAll().stream().filter(endpoint -> !endpoint.isDeactivated()).forEach(endpoint -> {
            final var iMqttClient = mqttClientManagementService.get(endpoint.asOnboardingResponse());
            if (iMqttClient.isEmpty()) {
                log.error(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.asOnboardingResponse().getSensorAlternateId()).asLogMessage());
            } else {
                final var listEndpointsService = new ListEndpointsServiceImpl(iMqttClient.get());
                final var parameters = new ListEndpointsParameters();
                parameters.setOnboardingResponse(endpoint.asOnboardingResponse());
                parameters.setDirection(Endpoints.ListEndpointsQuery.Direction.SEND);
                parameters.setTechnicalMessageType(SystemMessageType.EMPTY);
                parameters.setUnfilteredList(false);
                final var messageId = listEndpointsService.send(parameters);
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Scheduled recipient query for the endpoint.");

                log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
                MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
                messageWaitingForAcknowledgement.setAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
                messageWaitingForAcknowledgement.setMessageId(messageId);
                messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_LIST_ENDPOINTS.getKey());
                messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
            }
        });
    }
}
