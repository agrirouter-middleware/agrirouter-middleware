package de.agrirouter.middleware.business;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.business.parameters.VirtualOffboardProcessParameters;
import de.agrirouter.middleware.domain.enums.EndpointType;
import de.agrirouter.middleware.integration.VirtualOffboardProcessIntegrationService;
import de.agrirouter.middleware.integration.parameters.VirtualOffboardProcessIntegrationParameters;
import de.agrirouter.middleware.persistence.EndpointRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * The service for the onboard process.
 */
@Service
public class VirtualOffboardProcessService {

    private final EndpointRepository endpointRepository;
    private final VirtualOffboardProcessIntegrationService virtualOffboardProcessIntegrationService;
    private final EndpointService endpointService;

    public VirtualOffboardProcessService(EndpointRepository endpointRepository,
                                         VirtualOffboardProcessIntegrationService virtualOffboardProcessIntegrationService,
                                         EndpointService endpointService) {
        this.endpointRepository = endpointRepository;
        this.virtualOffboardProcessIntegrationService = virtualOffboardProcessIntegrationService;
        this.endpointService = endpointService;
    }

    /**
     * Offboard a virtual endpoint.
     *
     * @param virtualOffboardProcessParameters -
     */
    @Async
    @Transactional
    public void offboard(VirtualOffboardProcessParameters virtualOffboardProcessParameters) {
        virtualOffboardProcessParameters.getExternalVirtualEndpointIds().forEach(this::checkWhetherTheEndpointIsVirtualOrNot);
        final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDisabled(virtualOffboardProcessParameters.getExternalEndpointId());
        if (optionalEndpoint.isPresent() && !optionalEndpoint.get().isDeactivated()) {
            final var endpoint = optionalEndpoint.get();
            final var offboardVirtualEndpointParameters = new VirtualOffboardProcessIntegrationParameters();
            offboardVirtualEndpointParameters.setEndpoint(endpoint);
            offboardVirtualEndpointParameters.setEndpointIds(getEndpointIds(virtualOffboardProcessParameters));
            virtualOffboardProcessIntegrationService.offboard(offboardVirtualEndpointParameters);
            offboardVirtualEndpointParameters.getEndpointIds().forEach(endpointService::deleteEndpointDataFromTheMiddlewareByAgrirouterId);
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }

    private List<String> getEndpointIds(VirtualOffboardProcessParameters virtualOffboardProcessParameters) {
        final var agrirouterEndpointIds = new ArrayList<String>();
        virtualOffboardProcessParameters.getExternalVirtualEndpointIds().forEach(s -> {
            final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDisabled(s);
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
