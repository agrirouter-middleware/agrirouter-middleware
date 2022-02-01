package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.EndpointStatusUpdateEvent;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.EndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class ReconnectAllOnboardResponsesEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReconnectAllOnboardResponsesEventListener.class);

    private final EndpointRepository endpointRepository;
    private final MqttClientManagementService mqttClientManagementService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ReconnectAllOnboardResponsesEventListener(EndpointRepository endpointRepository,
                                                     MqttClientManagementService mqttClientManagementService,
                                                     ApplicationEventPublisher applicationEventPublisher) {
        this.endpointRepository = endpointRepository;
        this.mqttClientManagementService = mqttClientManagementService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Re-connect all endpoints after the application has started.
     *
     * @param applicationReadyEvent will be thrown after the application is ready.
     */
    @EventListener
    public void reconnectAllOnboardResponses(ApplicationReadyEvent applicationReadyEvent) {
        LOGGER.debug("The application is ready.");
        endpointRepository.findAll().stream().filter(endpoint -> !endpoint.isDeactivated()).forEach(endpoint -> {
            try {
                final var iMqttClient = mqttClientManagementService.get(endpoint.asOnboardingResponse());
                if (iMqttClient.isEmpty()) {
                    throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.asOnboardingResponse().getSensorAlternateId()));
                }
                final var onboardingResponse = endpoint.asOnboardingResponse();
                LOGGER.debug("Connect MQTT client for endpoint with the ID '{}' and client ID '{}'.", onboardingResponse.getSensorAlternateId(), onboardingResponse.getConnectionCriteria().getClientId());
                final var mqttClient = mqttClientManagementService.get(onboardingResponse);
                LOGGER.debug("MQTT client for endpoint with the ID '{}' and client ID '{}' connected", onboardingResponse.getSensorAlternateId(), iMqttClient.get().getClientId());
                applicationEventPublisher.publishEvent(new EndpointStatusUpdateEvent(this, endpoint.getAgrirouterEndpointId(), null));
            } catch (Exception e) {
                LOGGER.error("Could not reconnect a client, please check the client to avoid data loss.", e);
            }
        });
    }


}
