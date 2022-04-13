package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.events.EndpointStatusUpdateEvent;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.EndpointRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reconnect all existing onboard responses (if necessary).
 */
@Slf4j
@Component
public class ReconnectAllOnboardResponsesEventListener {

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
     */
    @EventListener(ApplicationReadyEvent.class)
    public void reconnectAllOnboardResponses() {
        log.debug("The application is ready.");
        endpointRepository.findAll().stream().filter(endpoint -> !endpoint.isDeactivated()).forEach(endpoint -> {
            try {
                final var iMqttClient = mqttClientManagementService.get(endpoint.asOnboardingResponse());
                if (iMqttClient.isEmpty()) {
                    log.error("Could not reconnect onboard response for endpoint {}.", endpoint.getExternalEndpointId());
                } else {
                    final var onboardingResponse = endpoint.asOnboardingResponse();
                    log.debug("Connect MQTT client for endpoint with the ID '{}' and client ID '{}'.", onboardingResponse.getSensorAlternateId(), onboardingResponse.getConnectionCriteria().getClientId());
                    final var mqttClient = mqttClientManagementService.get(onboardingResponse);
                    mqttClient.ifPresentOrElse(mc -> log.debug("MQTT client for endpoint with the ID '{}' and client ID '{}' connected", onboardingResponse.getSensorAlternateId(), mc.getClientId()), () ->
                            log.error("Could not reconnect a client, please check the client to avoid data loss."));
                    applicationEventPublisher.publishEvent(new EndpointStatusUpdateEvent(this, endpoint.getAgrirouterEndpointId(), null));
                }
            } catch (Exception e) {
                log.error("Could not reconnect a client, please check the client to avoid data loss.", e);
            }
        });
    }


}
