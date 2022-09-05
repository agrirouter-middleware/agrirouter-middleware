package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.DeactivateEndpointEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.persistence.EndpointRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Business operations regarding the endpoints.
 */
@Slf4j
@Service
public class DisableEndpointEventListener {

    private final EndpointRepository endpointRepository;
    private final BusinessOperationLogService businessOperationLogService;

    public DisableEndpointEventListener(EndpointRepository endpointRepository,
                                        BusinessOperationLogService businessOperationLogService) {
        this.endpointRepository = endpointRepository;
        this.businessOperationLogService = businessOperationLogService;
    }

    /**
     * Deactivate an endpoint after the event occurs.
     *
     * @param deactivateEndpointEvent -
     */
    @EventListener
    public void deactivateEndpoint(DeactivateEndpointEvent deactivateEndpointEvent) {
        final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(deactivateEndpointEvent.getAgrirouterEndpointId());
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            deactivateEndpoint(endpoint.getExternalEndpointId());
            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "The endpoint was deactivated.");
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }

    /**
     * Deactivate an endpoint.
     *
     * @param externalEndpointId -
     */
    private void deactivateEndpoint(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDeactivated(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            endpoint.setDeactivated(true);
            endpointRepository.save(endpoint);
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }

}
