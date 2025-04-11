package de.agrirouter.middleware.business;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.cache.cloud.CloudOnboardingFailureCache;
import de.agrirouter.middleware.business.parameters.VirtualOnboardProcessParameters;
import de.agrirouter.middleware.integration.VirtualOnboardProcessIntegrationService;
import de.agrirouter.middleware.integration.parameters.VirtualOnboardProcessIntegrationParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * The service for the onboard process.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VirtualOnboardProcessService {

    private final EndpointService endpointService;
    private final VirtualOnboardProcessIntegrationService virtualOnboardProcessIntegrationService;
    private final BusinessOperationLogService businessOperationLogService;
    private final CloudOnboardingFailureCache cloudOnboardingFailureCache;

    /**
     * Onboard a virtual endpoint.
     *
     * @param virtualOnboardProcessParameters -
     */
    public void onboard(VirtualOnboardProcessParameters virtualOnboardProcessParameters) {
        final var alreadyExistingEndpoint = endpointService.existsByExternalEndpointId(virtualOnboardProcessParameters.getExternalVirtualEndpointId());
        if (alreadyExistingEndpoint) {
            throw new BusinessException(ErrorMessageFactory.endpointWithTheSameExternalIdIsPresent(virtualOnboardProcessParameters.getExternalEndpointId()));
        } else {
            final var optionalEndpoint = endpointService.findByExternalEndpointId(virtualOnboardProcessParameters.getExternalEndpointId());
            if (optionalEndpoint.isPresent()) {
                var endpoint = optionalEndpoint.get();
                final var onboardVirtualEndpointParameters = new VirtualOnboardProcessIntegrationParameters(endpoint, virtualOnboardProcessParameters.getEndpointName(), virtualOnboardProcessParameters.getExternalVirtualEndpointId());
                virtualOnboardProcessIntegrationService.onboard(onboardVirtualEndpointParameters);
                cloudOnboardingFailureCache.clear(virtualOnboardProcessParameters.getExternalVirtualEndpointId());
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Virtual endpoint has been created.");
            } else {
                log.warn("Parent endpoint with external endpoint ID {} was not found.", virtualOnboardProcessParameters.getExternalEndpointId());
            }
        }
    }
}
