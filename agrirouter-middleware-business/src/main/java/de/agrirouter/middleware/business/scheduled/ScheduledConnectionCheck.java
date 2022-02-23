package de.agrirouter.middleware.business.scheduled;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.EndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled fetching and confirming of messages.
 */
@Component
public class ScheduledConnectionCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledConnectionCheck.class);

    private final EndpointRepository endpointRepository;
    private final MqttClientManagementService mqttClientManagementService;

    public ScheduledConnectionCheck(EndpointRepository endpointRepository,
                                    MqttClientManagementService mqttClientManagementService) {
        this.endpointRepository = endpointRepository;
        this.mqttClientManagementService = mqttClientManagementService;
    }

    /**
     * Schedule the connection check in a cyclic manner.
     */
    @Scheduled(cron = "${app.scheduled.connection-check}")
    public void connectionCheck() {
        LOGGER.debug("Scheduled connection check.");
        endpointRepository.findAll().stream().filter(endpoint -> !endpoint.isDeactivated()).forEach(endpoint -> {
            try {
                final var onboardingResponse = endpoint.asOnboardingResponse();
                final var iMqttClient = mqttClientManagementService.get(onboardingResponse);
                if (iMqttClient.isEmpty()) {
                    throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(onboardingResponse.getSensorAlternateId()));
                }
                final var existingiMqttClient = iMqttClient.get();
                if (existingiMqttClient.isConnected()) {
                    LOGGER.debug("Scheduled connection check for MQTT client '{}' was successful.", existingiMqttClient.getClientId());
                } else {
                    LOGGER.warn("Scheduled connection check for MQTT client '{}' has FAILED.", existingiMqttClient.getClientId());
                }
            } catch (BusinessException e) {
                LOGGER.error("Could not perform connection check for the endpoint '{}' since there was a business exception.", endpoint.getExternalEndpointId(), e);
            }
        });
    }
}
