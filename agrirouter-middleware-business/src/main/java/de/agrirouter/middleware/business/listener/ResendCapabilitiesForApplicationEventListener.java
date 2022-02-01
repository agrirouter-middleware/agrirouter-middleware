package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.ResendCapabilitiesForApplicationEvent;
import de.agrirouter.middleware.businesslog.BusinessLogService;
import de.agrirouter.middleware.integration.EndpointIntegrationService;
import de.agrirouter.middleware.persistence.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service for application maintenance.
 */
@Service
public class ResendCapabilitiesForApplicationEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResendCapabilitiesForApplicationEventListener.class);

    private final EndpointIntegrationService endpointIntegrationService;
    private final BusinessLogService businessLogService;
    private final ApplicationRepository applicationRepository;

    public ResendCapabilitiesForApplicationEventListener(EndpointIntegrationService endpointIntegrationService,
                                                         BusinessLogService businessLogService,
                                                         ApplicationRepository applicationRepository) {
        this.endpointIntegrationService = endpointIntegrationService;
        this.businessLogService = businessLogService;
        this.applicationRepository = applicationRepository;
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
            businessLogService.resendCapabilities(application);
            application.getEndpoints().forEach(endpoint -> endpointIntegrationService.sendCapabilities(application, endpoint));
        } else {
            LOGGER.error(ErrorMessageFactory.couldNotFindApplication().asLogMessage());
        }
    }
}
