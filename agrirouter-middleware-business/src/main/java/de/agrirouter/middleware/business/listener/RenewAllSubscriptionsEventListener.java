package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.events.UpdateSubscriptionsForEndpointEvent;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.integration.status.AgrirouterStatusIntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Remove messages waiting for acknowledgement which are older than a week.
 */
@Slf4j
@Component
public class RenewAllSubscriptionsEventListener {

    private final AgrirouterStatusIntegrationService agrirouterStatusIntegrationService;
    private final ApplicationService applicationService;
    private final EndpointService endpointService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public RenewAllSubscriptionsEventListener(AgrirouterStatusIntegrationService agrirouterStatusIntegrationService,
                                              ApplicationService applicationService, EndpointService endpointService,
                                              ApplicationEventPublisher applicationEventPublisher) {
        this.agrirouterStatusIntegrationService = agrirouterStatusIntegrationService;
        this.applicationService = applicationService;
        this.endpointService = endpointService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void clearAllMessagesWaitingForAck() {
        if (agrirouterStatusIntegrationService.isOperational()) {
            applicationService.findAll().forEach(application -> {
                log.debug("Renewing subscriptions for application with the ID {}.", application.getInternalApplicationId());
                endpointService.findAll(application.getInternalApplicationId()).forEach(endpoint -> {
                    log.debug("Renewing subscriptions for endpoint with the ID {}.", endpoint.getAgrirouterEndpointId());
                    applicationEventPublisher.publishEvent(new UpdateSubscriptionsForEndpointEvent(this, endpoint.getAgrirouterEndpointId()));
                });
            });
        } else {
            log.debug("Clearing all messages waiting for ACK skipped because agrirouter is not operational.");
        }
    }
}
