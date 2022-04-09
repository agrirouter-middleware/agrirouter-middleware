package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.DeactivateEndpointEvent;
import de.agrirouter.middleware.persistence.EndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Business operations regarding the endpoints.
 */
@Service
public class DisableEndpointEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisableEndpointEventListener.class);

    private final EndpointRepository endpointRepository;

    public DisableEndpointEventListener(EndpointRepository endpointRepository) {
        this.endpointRepository = endpointRepository;
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
        final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDisabled(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            LOGGER.warn("The endpoint with the id '{}' was deactivated.", endpoint.getAgrirouterEndpointId());
            endpoint.setDeactivated(true);
            endpointRepository.save(endpoint);
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }

}
