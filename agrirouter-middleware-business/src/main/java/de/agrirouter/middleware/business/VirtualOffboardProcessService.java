package de.agrirouter.middleware.business;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.business.parameters.VirtualOffboardProcessParameters;
import de.agrirouter.middleware.domain.enums.EndpointType;
import de.agrirouter.middleware.integration.VirtualOffboardProcessIntegrationService;
import de.agrirouter.middleware.integration.parameters.VirtualOffboardProcessIntegrationParameters;
import de.agrirouter.middleware.persistence.EndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The service for the onboard process.
 */
@Service
public class VirtualOffboardProcessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualOffboardProcessService.class);

    private final EndpointRepository endpointRepository;
    private final VirtualOffboardProcessIntegrationService virtualOffboardProcessIntegrationService;
    public VirtualOffboardProcessService(EndpointRepository endpointRepository,
                                         VirtualOffboardProcessIntegrationService virtualOffboardProcessIntegrationService) {
        this.endpointRepository = endpointRepository;
        this.virtualOffboardProcessIntegrationService = virtualOffboardProcessIntegrationService;
    }

    /**
     * Offboard a virtual endpoint.
     *
     * @param virtualOffboardProcessParameters -
     */
    public void offboard(VirtualOffboardProcessParameters virtualOffboardProcessParameters) {
        virtualOffboardProcessParameters.getExternalVirtualEndpointIds().forEach(this::checkWhetherTheEndpointIsVirtualOrNot);
        final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDeactivated(virtualOffboardProcessParameters.getExternalEndpointId());
        if (optionalEndpoint.isPresent() && !optionalEndpoint.get().isDeactivated()) {
            final var parentEndpoint = optionalEndpoint.get();
            final var virtualOffboardProcessIntegrationParameters = new VirtualOffboardProcessIntegrationParameters(parentEndpoint,getEndpointIds(virtualOffboardProcessParameters));
            virtualOffboardProcessIntegrationService.offboard(virtualOffboardProcessIntegrationParameters);
        } else {
            LOGGER.warn("Endpoint with external endpoint ID {} was not found.", virtualOffboardProcessParameters.getExternalEndpointId());
        }
    }

    private List<String> getEndpointIds(VirtualOffboardProcessParameters virtualOffboardProcessParameters) {
        final var agrirouterEndpointIds = new ArrayList<String>();
        virtualOffboardProcessParameters.getExternalVirtualEndpointIds().forEach(s -> {
            final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDeactivated(s);
            optionalEndpoint.ifPresent(endpoint -> agrirouterEndpointIds.add(endpoint.getAgrirouterEndpointId()));
        });
        return agrirouterEndpointIds;
    }

    private void checkWhetherTheEndpointIsVirtualOrNot(String endpointId) {
        final var optionalEndpoint = endpointRepository.findAllByExternalEndpointIdAndEndpointType(endpointId, EndpointType.VIRTUAL);
        if (optionalEndpoint.isEmpty()) {
            throw new BusinessException(ErrorMessageFactory.couldNotFindVirtualEndpoint());
        }
    }
}
