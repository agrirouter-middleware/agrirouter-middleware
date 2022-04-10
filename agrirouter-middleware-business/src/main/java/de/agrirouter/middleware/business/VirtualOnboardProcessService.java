package de.agrirouter.middleware.business;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.parameters.VirtualOnboardProcessParameters;
import de.agrirouter.middleware.integration.VirtualOnboardProcessIntegrationService;
import de.agrirouter.middleware.integration.parameters.VirtualOnboardProcessIntegrationParameters;
import de.agrirouter.middleware.persistence.EndpointRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * The service for the onboard process.
 */
@Service
public class VirtualOnboardProcessService {

    private final EndpointRepository endpointRepository;
    private final VirtualOnboardProcessIntegrationService virtualOnboardProcessIntegrationService;
    private final BusinessOperationLogService businessOperationLogService;

    public VirtualOnboardProcessService(EndpointRepository endpointRepository,
                                        VirtualOnboardProcessIntegrationService virtualOnboardProcessIntegrationService,
                                        BusinessOperationLogService businessOperationLogService) {
        this.endpointRepository = endpointRepository;
        this.virtualOnboardProcessIntegrationService = virtualOnboardProcessIntegrationService;
        this.businessOperationLogService = businessOperationLogService;
    }

    /**
     * Onboard a virtual endpoint.
     *
     * @param virtualOnboardProcessParameters -
     */
    @Async
    public void onboard(VirtualOnboardProcessParameters virtualOnboardProcessParameters) {
        final var alreadyExistingEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDisabled(virtualOnboardProcessParameters.getExternalVirtualEndpointId());
        alreadyExistingEndpoint.ifPresent(endpoint -> {
            throw new BusinessException(ErrorMessageFactory.endpointWithTheSameExternalIdIsPresent(endpoint.getExternalEndpointId()));
        });
        final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDisabled(virtualOnboardProcessParameters.getExternalEndpointId());
        if (optionalEndpoint.isPresent() && !optionalEndpoint.get().isDeactivated()) {
            final var endpoint = optionalEndpoint.get();
            final var onboardVirtualEndpointParameters = new VirtualOnboardProcessIntegrationParameters();
            onboardVirtualEndpointParameters.setEndpoint(endpoint);
            onboardVirtualEndpointParameters.setEndpointName(virtualOnboardProcessParameters.getEndpointName());
            onboardVirtualEndpointParameters.setExternalVirtualEndpointId(virtualOnboardProcessParameters.getExternalVirtualEndpointId());
            virtualOnboardProcessIntegrationService.onboard(onboardVirtualEndpointParameters);
            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()),"Virtual endpoint has been created.");
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }
}
