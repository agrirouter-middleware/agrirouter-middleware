package de.agrirouter.middleware.business;

import de.agrirouter.middleware.business.parameters.VirtualOffboardProcessParameters;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.VirtualOffboardProcessIntegrationService;
import de.agrirouter.middleware.integration.parameters.VirtualOffboardProcessIntegrationParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * The service for the onboard process.
 */
@Slf4j
@Service
public class VirtualOffboardProcessService {

    private final EndpointService endpointService;
    private final VirtualOffboardProcessIntegrationService virtualOffboardProcessIntegrationService;

    public VirtualOffboardProcessService(EndpointService endpointService,
                                         VirtualOffboardProcessIntegrationService virtualOffboardProcessIntegrationService) {
        this.endpointService = endpointService;
        this.virtualOffboardProcessIntegrationService = virtualOffboardProcessIntegrationService;
    }

    /**
     * Offboard a virtual endpoint.
     *
     * @param virtualOffboardProcessParameters -
     */
    public void offboard(VirtualOffboardProcessParameters virtualOffboardProcessParameters) {
        final var parentEndpoint = endpointService.findByExternalEndpointId(virtualOffboardProcessParameters.getExternalEndpointId());
        if (parentEndpoint.isPresent()) {
            final var virtualOffboardProcessIntegrationParameters = new VirtualOffboardProcessIntegrationParameters(parentEndpoint, getEndpointIds(virtualOffboardProcessParameters));
            if (CollectionUtils.isEmpty(virtualOffboardProcessIntegrationParameters.virtualEndpointIds())) {
                log.warn("No virtual endpoints available, therefore we are skipping the process.");
            } else {
                virtualOffboardProcessIntegrationService.offboard(virtualOffboardProcessIntegrationParameters);
            }
        } else {
            log.warn("Parent endpoint with external endpoint ID {} was not found.", virtualOffboardProcessParameters.getExternalEndpointId());
        }
    }

    private List<String> getEndpointIds(VirtualOffboardProcessParameters virtualOffboardProcessParameters) {
        final var agrirouterEndpointIds = endpointService.findByExternalEndpointIds(virtualOffboardProcessParameters.getExternalVirtualEndpointIds())
                .stream()
                .map(Endpoint::getAgrirouterEndpointId)
                .toList();
        log.debug("Found {} virtual endpoints.", agrirouterEndpointIds.size());
        log.trace("Virtual endpoint IDs: {}", String.join(",", agrirouterEndpointIds));
        return agrirouterEndpointIds;
    }

}
