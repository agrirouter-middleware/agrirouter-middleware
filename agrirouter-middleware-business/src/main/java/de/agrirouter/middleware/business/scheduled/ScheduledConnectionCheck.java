package de.agrirouter.middleware.business.scheduled;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.EndpointRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled fetching and confirming of messages.
 */
@Slf4j
@Component
public class ScheduledConnectionCheck {

    private final EndpointRepository endpointRepository;
    private final MqttClientManagementService mqttClientManagementService;
    private final BusinessOperationLogService businessOperationLogService;

    public ScheduledConnectionCheck(EndpointRepository endpointRepository,
                                    MqttClientManagementService mqttClientManagementService,
                                    BusinessOperationLogService businessOperationLogService) {
        this.endpointRepository = endpointRepository;
        this.mqttClientManagementService = mqttClientManagementService;
        this.businessOperationLogService = businessOperationLogService;
    }

    /**
     * Schedule the connection check in a cyclic manner.
     */
    @Scheduled(cron = "${app.scheduled.connection-check}")
    public void connectionCheck() {
        log.debug("Scheduled connection check.");
        endpointRepository.findAll().stream().filter(endpoint -> !endpoint.isDeactivated()).forEach(endpoint -> {
            try {
                final var onboardingResponse = endpoint.asOnboardingResponse();
                final var iMqttClient = mqttClientManagementService.get(onboardingResponse);
                if (iMqttClient.isEmpty()) {
                    throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(onboardingResponse.getSensorAlternateId()));
                }
                final var existingiMqttClient = iMqttClient.get();
                if (existingiMqttClient.isConnected()) {
                    log.debug("Scheduled connection check for MQTT client '{}' was successful.", existingiMqttClient.getClientId());
                } else {
                    log.warn("Scheduled connection check for MQTT client '{}' has FAILED.", existingiMqttClient.getClientId());
                }
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Scheduled connection check was successful.");
            } catch (BusinessException e) {
                log.error("Could not perform connection check for the endpoint '{}' since there was a business exception.", endpoint.getExternalEndpointId(), e);
            }
        });
    }
}
