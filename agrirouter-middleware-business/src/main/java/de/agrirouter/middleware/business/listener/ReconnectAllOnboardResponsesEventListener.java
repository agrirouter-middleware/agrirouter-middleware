package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.business.ApplicationService;
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

    private final ApplicationService applicationService;
    private final EndpointService endpointService;
    private final MqttClientManagementService mqttClientManagementService;

    public ReconnectAllOnboardResponsesEventListener(ApplicationService applicationService,
                                                     EndpointService endpointService,
                                                     MqttClientManagementService mqttClientManagementService) {
        this.applicationService = applicationService;
        this.endpointService = endpointService;
        this.mqttClientManagementService = mqttClientManagementService;
    }

    /**
     * Re-connect all endpoints after the application has started.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void reconnectAllOnboardResponses() {
        log.debug("Reconnect all endpoints to receive messages from the agrirouter.");
        updateEndpoints();
        endpointService.findAll().stream().filter(endpoint -> !endpoint.isDeactivated()).forEach(endpoint -> {
            try {
                final var iMqttClient = mqttClientManagementService.get(endpoint);
                if (iMqttClient.isEmpty()) {
                    log.error("Could not reconnect onboard response for endpoint {}.", endpoint.getExternalEndpointId());
                } else {
                    final var onboardingResponse = endpoint.asOnboardingResponse();
                    log.debug("Connect MQTT client for endpoint with the ID '{}' and client ID '{}'.", endpoint.getAgrirouterEndpointId(), onboardingResponse.getConnectionCriteria().getClientId());
                    final var mqttClient = mqttClientManagementService.get(endpoint);
                    mqttClient.ifPresentOrElse(mc -> log.debug("MQTT client for endpoint with the ID '{}' and client ID '{}' connected", onboardingResponse.getSensorAlternateId(), mc.getClientId()), () ->
                            log.error("Could not reconnect a client, please check the client to avoid data loss."));
                }
            } catch (Exception e) {
                log.error("Could not reconnect a client, please check the client to avoid data loss.", e);
            }
        });
    }

    private void updateEndpoints() {
        applicationService.findAll().forEach(application -> {
            if (application.usesRouterDevice()) {
                application.getEndpoints().forEach(endpoint -> {
                    if (!endpoint.usesRouterDevice()) {
                        log.debug("Update endpoint {} to use router device.", endpoint.getExternalEndpointId());
                        endpoint.setOnboardResponseForRouterDevice(application.createOnboardResponseForRouterDevice(endpoint.asOnboardingResponse(true)));
                        endpointService.save(endpoint);
                    } else {
                        log.debug("Endpoint {} already uses router device. Skipped the update.", endpoint.getExternalEndpointId());
                    }
                });
            } else {
                log.error(ErrorMessageFactory.missingRouterDeviceForApplication(application.getInternalApplicationId()).asLogMessage());
            }
        });
    }


}
