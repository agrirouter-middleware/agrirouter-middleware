package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.exception.OnboardingException;
import com.google.gson.Gson;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.business.parameters.OnboardProcessParameters;
import de.agrirouter.middleware.businesslog.BusinessLogService;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.OnboardProcessIntegrationService;
import de.agrirouter.middleware.integration.parameters.OnboardProcessIntegrationParameters;
import de.agrirouter.middleware.persistence.ApplicationRepository;
import de.agrirouter.middleware.persistence.EndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The service for the onboard process.
 */
@Service
public class OnboardProcessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardProcessService.class);

    private final EndpointRepository endpointRepository;
    private final OnboardProcessIntegrationService onboardProcessIntegrationService;
    private final BusinessLogService businessLogService;
    private final ApplicationRepository applicationRepository;
    private final EndpointService endpointService;

    public OnboardProcessService(EndpointRepository endpointRepository,
                                 OnboardProcessIntegrationService onboardProcessIntegrationService,
                                 BusinessLogService businessLogService,
                                 ApplicationRepository applicationRepository,
                                 EndpointService endpointService) {
        this.endpointRepository = endpointRepository;
        this.onboardProcessIntegrationService = onboardProcessIntegrationService;
        this.businessLogService = businessLogService;
        this.applicationRepository = applicationRepository;
        this.endpointService = endpointService;
    }

    /**
     * Onboard process for common telemetry connections, like CUs.
     *
     * @param onboardProcessParameters -
     */
    public void onboard(OnboardProcessParameters onboardProcessParameters) {
        try {
            final var optionalApplication = applicationRepository.findByInternalApplicationIdAndTenantTenantId(onboardProcessParameters.getInternalApplicationId(), onboardProcessParameters.getTenantId());
            if (optionalApplication.isPresent()) {
                final var existingEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDisabled(onboardProcessParameters.getExternalEndpointId());
                final var application = optionalApplication.get();
                if (existingEndpoint.isPresent()) {
                    LOGGER.debug("Updating existing endpoint, this was a onboard process for an existing endpoint.");
                    final var endpoint = existingEndpoint.get();

                    final var onboardProcessIntegrationParameters = new OnboardProcessIntegrationParameters();
                    onboardProcessIntegrationParameters.setEndpointId(endpoint.getExternalEndpointId());
                    onboardProcessIntegrationParameters.setApplicationId(application.getApplicationId());
                    onboardProcessIntegrationParameters.setVersionId(application.getVersionId());
                    onboardProcessIntegrationParameters.setRegistrationCode(onboardProcessParameters.getRegistrationCode());
                    final var onboardingResponse = onboardProcessIntegrationService.onboard(onboardProcessIntegrationParameters);

                    endpoint.setOnboardResponse(new Gson().toJson(onboardingResponse));
                    endpointRepository.save(endpoint);
                    businessLogService.onboardEndpointAgain(endpoint);
                    endpointService.sendCapabilities(application, endpoint);
                } else {
                    LOGGER.debug("Create a new endpoint, since the endpoint does not exist in the database.");
                    final var onboardProcessIntegrationParameters = new OnboardProcessIntegrationParameters();
                    onboardProcessIntegrationParameters.setEndpointId(onboardProcessParameters.getExternalEndpointId());
                    onboardProcessIntegrationParameters.setApplicationId(application.getApplicationId());
                    onboardProcessIntegrationParameters.setVersionId(application.getVersionId());
                    onboardProcessIntegrationParameters.setRegistrationCode(onboardProcessParameters.getRegistrationCode());
                    final var onboardingResponse = onboardProcessIntegrationService.onboard(onboardProcessIntegrationParameters);

                    final var endpoint = new Endpoint();
                    endpoint.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
                    endpoint.setExternalEndpointId(onboardProcessParameters.getExternalEndpointId());
                    endpoint.setOnboardResponse(new Gson().toJson(onboardingResponse));
                    endpoint.setOnboardResponse(application.createOnboardResponseForRouterDevice(endpoint.asOnboardingResponse(true)));
                    endpointRepository.save(endpoint);
                    application.getEndpoints().add(endpoint);
                    applicationRepository.save(application);
                    businessLogService.onboardEndpoint(endpoint);
                    endpointService.sendCapabilities(application, endpoint);
                }
            } else {
                throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
            }
        } catch (OnboardingException e) {
            LOGGER.error("[{}] {}", ErrorMessageFactory.onboardRequestFailed().getKey(), ErrorMessageFactory.onboardRequestFailed().getMessage());
            throw new BusinessException(ErrorMessageFactory.onboardRequestFailed(), e);
        }
    }

}
