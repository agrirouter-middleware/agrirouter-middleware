package de.agrirouter.middleware.business.scheduled;

import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.parameters.MessageQueryParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.MessageHeaderQueryServiceImpl;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.status.AgrirouterStatusIntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Scheduled status logging for each endpoint.
 */
@Slf4j
@Component
public class ScheduledStatusLogging {

    private final EndpointService endpointService;
    private final MqttClientManagementService mqttClientManagementService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final BusinessOperationLogService businessOperationLogService;
    private final AgrirouterStatusIntegrationService agrirouterStatusIntegrationService;

    public ScheduledStatusLogging(EndpointService endpointService,
                                  MqttClientManagementService mqttClientManagementService,
                                  MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                  BusinessOperationLogService businessOperationLogService,
                                  AgrirouterStatusIntegrationService agrirouterStatusIntegrationService) {
        this.endpointService = endpointService;
        this.mqttClientManagementService = mqttClientManagementService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.businessOperationLogService = businessOperationLogService;
        this.agrirouterStatusIntegrationService = agrirouterStatusIntegrationService;
    }

    /**
     * Schedule the status logging in a cyclic manner.
     */
    @Scheduled(cron = "${app.scheduled.status-logging}")
    public void scheduledStatusLogging() {
        if (agrirouterStatusIntegrationService.isOperational()) {
            log.debug("Scheduled status update for endpoints.");
            endpointService.findAll().stream().filter(endpoint -> !endpoint.isDeactivated()).forEach(endpoint -> {
                final var iMqttClient = mqttClientManagementService.get(endpoint.asOnboardingResponse());
                if (iMqttClient.isEmpty()) {
                    log.error(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.asOnboardingResponse().getSensorAlternateId()).asLogMessage());
                } else {
                    final var messageHeaderQueryService = new MessageHeaderQueryServiceImpl(iMqttClient.get());
                    final var parameters = new MessageQueryParameters();
                    parameters.setSentFromInSeconds(Instant.now().minus(28, ChronoUnit.DAYS).getEpochSecond());
                    parameters.setSentToInSeconds(Instant.now().getEpochSecond());
                    parameters.setOnboardingResponse(endpoint.asOnboardingResponse());
                    final var messageId = messageHeaderQueryService.send(parameters);
                    businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Scheduled status update for the endpoint.");

                    log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
                    MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
                    messageWaitingForAcknowledgement.setAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
                    messageWaitingForAcknowledgement.setMessageId(messageId);
                    messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_FEED_HEADER_QUERY.getKey());
                    messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
                }
            });
        } else {
            log.debug("Agrirouter is not operational. Skipping scheduled status update.");
        }
    }
}
