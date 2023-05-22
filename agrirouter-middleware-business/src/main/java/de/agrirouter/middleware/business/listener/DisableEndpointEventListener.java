package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.events.DeactivateEndpointEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.EndpointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Business operations regarding the endpoints.
 */
@Slf4j
@Service
public class DisableEndpointEventListener {

    private final EndpointService endpointService;
    private final BusinessOperationLogService businessOperationLogService;

    public DisableEndpointEventListener(EndpointService endpointService,
                                        BusinessOperationLogService businessOperationLogService) {
        this.endpointService = endpointService;
        this.businessOperationLogService = businessOperationLogService;
    }

    /**
     * Deactivate an endpoint after the event occurs.
     *
     * @param deactivateEndpointEvent -
     */
    @EventListener
    public void deactivateEndpoint(DeactivateEndpointEvent deactivateEndpointEvent) {
        final var endpoint = endpointService.findByAgrirouterEndpointId(deactivateEndpointEvent.getAgrirouterEndpointId());
        endpoint.setDeactivated(true);
        endpointService.save(endpoint);
        businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "The endpoint was deactivated.");
    }

}
