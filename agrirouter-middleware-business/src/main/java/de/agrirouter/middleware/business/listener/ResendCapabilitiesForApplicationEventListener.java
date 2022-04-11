package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.ResendCapabilitiesForApplicationEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.integration.EndpointIntegrationService;
import de.agrirouter.middleware.persistence.ApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service for application maintenance.
 */
@Slf4j
@Service
public class ResendCapabilitiesForApplicationEventListener {

    private final EndpointIntegrationService endpointIntegrationService;
    private final ApplicationRepository applicationRepository;
    private final BusinessOperationLogService businessOperationLogService;

    public ResendCapabilitiesForApplicationEventListener(EndpointIntegrationService endpointIntegrationService,
                                                         ApplicationRepository applicationRepository,
                                                         BusinessOperationLogService businessOperationLogService) {
        this.endpointIntegrationService = endpointIntegrationService;
        this.applicationRepository = applicationRepository;
        this.businessOperationLogService = businessOperationLogService;
    }

    /**
     * Send capabilities for each endpoint.
     *
     * @param resendCapabilitiesForApplicationEvent The event containing the application.
     */
    @EventListener
    public void resendCapabilities(ResendCapabilitiesForApplicationEvent resendCapabilitiesForApplicationEvent) {
        final var optionalApplication = applicationRepository.findByInternalApplicationId(resendCapabilitiesForApplicationEvent.getInternalApplicationId());
        if (optionalApplication.isPresent()) {
            final var application = optionalApplication.get();
            application.getEndpoints().forEach(endpoint -> {
                endpointIntegrationService.sendCapabilities(application, endpoint);
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Sending capabilities for the endpoint.");
            });
        } else {
            log.error(ErrorMessageFactory.couldNotFindApplication().asLogMessage());
        }
    }
}
