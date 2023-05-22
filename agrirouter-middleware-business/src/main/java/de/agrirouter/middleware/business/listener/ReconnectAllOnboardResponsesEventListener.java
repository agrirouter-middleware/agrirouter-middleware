package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reconnect all existing onboard responses (if necessary).
 */
@Slf4j
@Component
public class ReconnectAllOnboardResponsesEventListener {

    private final EndpointService endpointService;
    private final MqttClientManagementService mqttClientManagementService;

    public ReconnectAllOnboardResponsesEventListener(EndpointService endpointService,
                                                     MqttClientManagementService mqttClientManagementService) {
        this.endpointService = endpointService;
        this.mqttClientManagementService = mqttClientManagementService;
    }

    /**
     * Re-connect all endpoints after the application has started.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void reconnectAllOnboardResponses() {
        log.debug("The application is ready.");
        endpointService.findAll().stream().filter(endpoint -> !endpoint.isDeactivated()).forEach(endpoint -> {
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
                }
            } catch (Exception e) {
                log.error("Could not reconnect a client, please check the client to avoid data loss.", e);
            }
        });
    }


}
