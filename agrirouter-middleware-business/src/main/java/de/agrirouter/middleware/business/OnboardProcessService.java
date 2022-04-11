package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.exception.OnboardingException;
import com.google.gson.Gson;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.ApplicationLogInformation;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.parameters.OnboardProcessParameters;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.OnboardProcessIntegrationService;
import de.agrirouter.middleware.integration.parameters.OnboardProcessIntegrationParameters;
import de.agrirouter.middleware.persistence.ApplicationRepository;
import de.agrirouter.middleware.persistence.EndpointRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * The service for the onboard process.
 */
@Slf4j
@Service
public class OnboardProcessService {

    private final EndpointRepository endpointRepository;
    private final OnboardProcessIntegrationService onboardProcessIntegrationService;
    private final ApplicationRepository applicationRepository;
    private final EndpointService endpointService;
    private final BusinessOperationLogService businessOperationLogService;

    public OnboardProcessService(EndpointRepository endpointRepository,
                                 OnboardProcessIntegrationService onboardProcessIntegrationService,
                                 ApplicationRepository applicationRepository,
                                 EndpointService endpointService,
                                 BusinessOperationLogService businessOperationLogService) {
        this.endpointRepository = endpointRepository;
        this.onboardProcessIntegrationService = onboardProcessIntegrationService;
        this.applicationRepository = applicationRepository;
        this.endpointService = endpointService;
        this.businessOperationLogService = businessOperationLogService;
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
                    log.debug("Updating existing endpoint, this was a onboard process for an existing endpoint.");
                    final var endpoint = existingEndpoint.get();

                    final var onboardProcessIntegrationParameters = new OnboardProcessIntegrationParameters();
                    onboardProcessIntegrationParameters.setEndpointId(endpoint.getExternalEndpointId());
                    onboardProcessIntegrationParameters.setApplicationId(application.getApplicationId());
                    onboardProcessIntegrationParameters.setVersionId(application.getVersionId());
                    onboardProcessIntegrationParameters.setRegistrationCode(onboardProcessParameters.getRegistrationCode());
                    final var onboardingResponse = onboardProcessIntegrationService.onboard(onboardProcessIntegrationParameters);
                    endpoint.setOnboardResponse(new Gson().toJson(onboardingResponse));
                    endpointRepository.save(endpoint);
                    businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "The endpoint was updated.");
                    endpointService.sendCapabilities(application, endpoint);
                } else {
                    log.debug("Create a new endpoint, since the endpoint does not exist in the database.");
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
                    businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "The endpoint was created.");
                    application.getEndpoints().add(endpoint);
                    applicationRepository.save(application);
                    businessOperationLogService.log(new ApplicationLogInformation(application.getInternalApplicationId(), application.getApplicationId()), "The endpoint was added to the application.");
                    endpointService.sendCapabilities(application, endpoint);
                }
            } else {
                throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
            }
        } catch (OnboardingException e) {
            log.error("[{}] {}", ErrorMessageFactory.onboardRequestFailed().getKey(), ErrorMessageFactory.onboardRequestFailed().getMessage());
            throw new BusinessException(ErrorMessageFactory.onboardRequestFailed(), e);
        }
    }

}
