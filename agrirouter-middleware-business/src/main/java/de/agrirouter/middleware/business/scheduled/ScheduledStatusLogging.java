package de.agrirouter.middleware.business.scheduled;

import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.parameters.MessageQueryParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.MessageHeaderQueryServiceImpl;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Scheduled status logging for each endpoint.
 */
@Component
public class ScheduledStatusLogging {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledStatusLogging.class);

    private final EndpointRepository endpointRepository;
    private final MqttClientManagementService mqttClientManagementService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;

    public ScheduledStatusLogging(EndpointRepository endpointRepository,
                                  MqttClientManagementService mqttClientManagementService,
                                  MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService) {
        this.endpointRepository = endpointRepository;
        this.mqttClientManagementService = mqttClientManagementService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
    }

    /**
     * Schedule the status logging in a cyclic manner.
     */
    @Scheduled(cron = "${app.scheduled.status-logging}")
    public void scheduledStatusLogging() {
        LOGGER.debug("Scheduled status update for endpoints.");
        endpointRepository.findAll().stream().filter(endpoint -> !endpoint.isDeactivated()).forEach(endpoint -> {
            final var iMqttClient = mqttClientManagementService.get(endpoint.asOnboardingResponse());
            if (iMqttClient.isEmpty()) {
                throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.asOnboardingResponse().getSensorAlternateId()));
            }
            final var messageHeaderQueryService = new MessageHeaderQueryServiceImpl(iMqttClient.get());
            final var parameters = new MessageQueryParameters();
            parameters.setSentFromInSeconds(Instant.now().minus(28, ChronoUnit.DAYS).getEpochSecond());
            parameters.setSentToInSeconds(Instant.now().getEpochSecond());
            parameters.setOnboardingResponse(endpoint.asOnboardingResponse());
            final var messageId = messageHeaderQueryService.send(parameters);

            LOGGER.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
            MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
            messageWaitingForAcknowledgement.setAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
            messageWaitingForAcknowledgement.setMessageId(messageId);
            messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_FEED_HEADER_QUERY.getKey());
            messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
        });
    }
}
