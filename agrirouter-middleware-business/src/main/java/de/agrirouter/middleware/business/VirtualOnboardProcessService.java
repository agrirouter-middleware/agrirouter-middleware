package de.agrirouter.middleware.business;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.cache.cloud.CloudOnboardingFailureCache;
import de.agrirouter.middleware.business.parameters.VirtualOnboardProcessParameters;
import de.agrirouter.middleware.integration.VirtualOnboardProcessIntegrationService;
import de.agrirouter.middleware.integration.parameters.VirtualOnboardProcessIntegrationParameters;
import org.springframework.stereotype.Service;

/**
 * The service for the onboard process.
 */
@Service
public class VirtualOnboardProcessService {

    private final EndpointService endpointService;
    private final VirtualOnboardProcessIntegrationService virtualOnboardProcessIntegrationService;
    private final BusinessOperationLogService businessOperationLogService;
    private final CloudOnboardingFailureCache cloudOnboardingFailureCache;

    public VirtualOnboardProcessService(EndpointService endpointService,
                                        VirtualOnboardProcessIntegrationService virtualOnboardProcessIntegrationService,
                                        BusinessOperationLogService businessOperationLogService,
                                        CloudOnboardingFailureCache cloudOnboardingFailureCache) {
        this.endpointService = endpointService;
        this.virtualOnboardProcessIntegrationService = virtualOnboardProcessIntegrationService;
        this.businessOperationLogService = businessOperationLogService;
        this.cloudOnboardingFailureCache = cloudOnboardingFailureCache;
    }

    /**
     * Onboard a virtual endpoint.
     *
     * @param virtualOnboardProcessParameters -
     */
    public void onboard(VirtualOnboardProcessParameters virtualOnboardProcessParameters) {
        final var alreadyExistingEndpoint = endpointService.findByExternalEndpointId(virtualOnboardProcessParameters.getExternalVirtualEndpointId());
        if (alreadyExistingEndpoint != null) {
            throw new BusinessException(ErrorMessageFactory.endpointWithTheSameExternalIdIsPresent(alreadyExistingEndpoint.getExternalEndpointId()));
        }
        final var endpoint = endpointService.findByExternalEndpointId(virtualOnboardProcessParameters.getExternalEndpointId());
        final var onboardVirtualEndpointParameters = new VirtualOnboardProcessIntegrationParameters(endpoint, virtualOnboardProcessParameters.getEndpointName(), virtualOnboardProcessParameters.getExternalVirtualEndpointId());
        virtualOnboardProcessIntegrationService.onboard(onboardVirtualEndpointParameters);
        cloudOnboardingFailureCache.clear(virtualOnboardProcessParameters.getExternalVirtualEndpointId());
        businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Virtual endpoint has been created.");
    }
}
