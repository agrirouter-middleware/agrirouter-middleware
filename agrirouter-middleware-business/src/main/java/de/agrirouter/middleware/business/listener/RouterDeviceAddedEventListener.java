package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.RouterDeviceAddedEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.ApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service for application maintenance.
 */
@Slf4j
@Service
public class RouterDeviceAddedEventListener {

    private final ApplicationRepository applicationRepository;
    private final MqttClientManagementService mqttClientManagementService;
    private final BusinessOperationLogService businessOperationLogService;
    private final EndpointService endpointService;

    public RouterDeviceAddedEventListener(ApplicationRepository applicationRepository,
                                          MqttClientManagementService mqttClientManagementService,
                                          BusinessOperationLogService businessOperationLogService,
                                          EndpointService endpointService) {
        this.applicationRepository = applicationRepository;
        this.mqttClientManagementService = mqttClientManagementService;
        this.businessOperationLogService = businessOperationLogService;
        this.endpointService = endpointService;
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
                application.getEndpoints().forEach(endpoint -> {
                    endpoint.setOnboardResponseForRouterDevice(application.createOnboardResponseForRouterDevice(endpoint.asOnboardingResponse(true)));
                    endpointService.save(endpoint);
                    businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Update endpoint information. The endpoint is now using the router device.");
                });
                log.debug("Forcefully disconnect existing clients to prepare the usage of the router device.");
                application.getEndpoints().forEach(endpoint -> {
                    mqttClientManagementService.disconnectForOriginalOnboardingResponse(endpoint);
                    businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Disconnect endpoint to force usage of the router device.");
                });
            } else {
                log.error(ErrorMessageFactory.missingRouterDeviceForApplication(application.getInternalApplicationId()).asLogMessage());
            }
        } else {
            log.error(ErrorMessageFactory.couldNotFindApplication().asLogMessage());
        }
    }
}
