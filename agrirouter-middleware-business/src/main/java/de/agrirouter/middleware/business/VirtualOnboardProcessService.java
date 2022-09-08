package de.agrirouter.middleware.business;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.cache.cloud.CloudOnboardingFailureCache;
import de.agrirouter.middleware.business.parameters.VirtualOnboardProcessParameters;
import de.agrirouter.middleware.integration.VirtualOnboardProcessIntegrationService;
import de.agrirouter.middleware.integration.parameters.VirtualOnboardProcessIntegrationParameters;
import de.agrirouter.middleware.persistence.EndpointRepository;
import org.springframework.stereotype.Service;

/**
 * The service for the onboard process.
 */
@Service
public class VirtualOnboardProcessService {

    private final EndpointRepository endpointRepository;
    private final VirtualOnboardProcessIntegrationService virtualOnboardProcessIntegrationService;
    private final BusinessOperationLogService businessOperationLogService;
    private final CloudOnboardingFailureCache cloudOnboardingFailureCache;

    public VirtualOnboardProcessService(EndpointRepository endpointRepository,
                                        VirtualOnboardProcessIntegrationService virtualOnboardProcessIntegrationService,
                                        BusinessOperationLogService businessOperationLogService,
                                        CloudOnboardingFailureCache cloudOnboardingFailureCache) {
        this.endpointRepository = endpointRepository;
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
        final var alreadyExistingEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDeactivated(virtualOnboardProcessParameters.getExternalVirtualEndpointId());
        alreadyExistingEndpoint.ifPresent(endpoint -> {
            throw new BusinessException(ErrorMessageFactory.endpointWithTheSameExternalIdIsPresent(endpoint.getExternalEndpointId()));
        });
        final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDeactivated(virtualOnboardProcessParameters.getExternalEndpointId());
        if (optionalEndpoint.isPresent() && !optionalEndpoint.get().isDeactivated()) {
            final var parentEndpoint = optionalEndpoint.get();
            final var onboardVirtualEndpointParameters = new VirtualOnboardProcessIntegrationParameters(parentEndpoint,virtualOnboardProcessParameters.getEndpointName(), virtualOnboardProcessParameters.getExternalVirtualEndpointId());
            virtualOnboardProcessIntegrationService.onboard(onboardVirtualEndpointParameters);
            cloudOnboardingFailureCache.clear(virtualOnboardProcessParameters.getExternalVirtualEndpointId());
            businessOperationLogService.log(new EndpointLogInformation(parentEndpoint.getExternalEndpointId(), parentEndpoint.getAgrirouterEndpointId()),"Virtual endpoint has been created.");
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }
}
