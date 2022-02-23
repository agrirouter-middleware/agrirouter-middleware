package de.agrirouter.middleware.business.scheduled;

import agrirouter.request.payload.account.Endpoints;
import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.parameters.ListEndpointsParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.ListEndpointsServiceImpl;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.EndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled fetching of possible recipients.
 */
@Component
public class ScheduledRecipientQuery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledRecipientQuery.class);

    private final EndpointRepository endpointRepository;
    private final MqttClientManagementService mqttClientManagementService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;

    public ScheduledRecipientQuery(EndpointRepository endpointRepository,
                                   MqttClientManagementService mqttClientManagementService,
                                   MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService) {
        this.endpointRepository = endpointRepository;
        this.mqttClientManagementService = mqttClientManagementService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
    }

    /**
     * Schedule the recipient checking in a cyclic manner.
     */
    @Scheduled(cron = "${app.scheduled.status-logging}")
    public void scheduledStatusLogging() {
        LOGGER.debug("Scheduled recipient checking for all endpoints.");
        endpointRepository.findAll().stream().filter(endpoint -> !endpoint.isDeactivated()).forEach(endpoint -> {
            final var iMqttClient = mqttClientManagementService.get(endpoint.asOnboardingResponse());
            if (iMqttClient.isEmpty()) {
                throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.asOnboardingResponse().getSensorAlternateId()));
            }
            final var listEndpointsService = new ListEndpointsServiceImpl(iMqttClient.get());
            final var parameters = new ListEndpointsParameters();
            parameters.setOnboardingResponse(endpoint.asOnboardingResponse());
            parameters.setDirection(Endpoints.ListEndpointsQuery.Direction.RECEIVE);
            parameters.setTechnicalMessageType(SystemMessageType.EMPTY);
            parameters.setUnfilteredList(false);
            final var messageId = listEndpointsService.send(parameters);

            LOGGER.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
            MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
            messageWaitingForAcknowledgement.setAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
            messageWaitingForAcknowledgement.setMessageId(messageId);
            messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_LIST_ENDPOINTS.getKey());
            messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
        });
    }
}
