package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.RouterDeviceAddedEvent;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service for application maintenance.
 */
@Service
public class RouterDeviceAddedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterDeviceAddedEventListener.class);

    private final ApplicationRepository applicationRepository;
    private final MqttClientManagementService mqttClientManagementService;

    public RouterDeviceAddedEventListener(ApplicationRepository applicationRepository,
                                          MqttClientManagementService mqttClientManagementService) {
        this.applicationRepository = applicationRepository;
        this.mqttClientManagementService = mqttClientManagementService;
    }

    /**
     * Send capabilities for each endpoint.
     *
     * @param resendCapabilitiesForApplicationEvent The event containing the application.
     */
    @EventListener
    public void updateEndpoints(RouterDeviceAddedEvent resendCapabilitiesForApplicationEvent) {
        final var optionalApplication = applicationRepository.findByInternalApplicationId(resendCapabilitiesForApplicationEvent.getInternalApplicationId());
        if (optionalApplication.isPresent()) {
            final var application = optionalApplication.get();
            if (application.usesRouterDevice()) {
                application.getEndpoints().forEach(endpoint -> endpoint.setOnboardResponseForRouterDevice(application.createOnboardResponseForRouterDevice(endpoint.asOnboardingResponse(true))));
                LOGGER.debug("Forcefully disconnect existing clients to prepare the usage of the router device.");
                application.getEndpoints().forEach(endpoint -> mqttClientManagementService.disconnect(endpoint.asOnboardingResponse(true)));
            } else {
                LOGGER.error(ErrorMessageFactory.missingRouterDeviceForApplication().asLogMessage());
            }
        } else {
            LOGGER.error(ErrorMessageFactory.couldNotFindApplication().asLogMessage());
        }
    }
}
