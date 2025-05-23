package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.SecuredOnboardingResponseType;
import com.dke.data.agrirouter.api.exception.OnboardingException;
import com.dke.data.agrirouter.api.service.onboard.secured.AuthorizationRequestService;
import com.dke.data.agrirouter.api.service.parameters.AuthorizationRequestParameters;
import com.google.gson.Gson;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.ApplicationLogInformation;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.global.OnboardStateContainer;
import de.agrirouter.middleware.business.parameters.OnboardProcessParameters;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.SecuredOnboardProcessIntegrationService;
import de.agrirouter.middleware.integration.parameters.SecuredOnboardProcessIntegrationParameters;
import de.agrirouter.middleware.persistence.jpa.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The service for the onboard process.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecuredOnboardProcessService {

    private final AuthorizationRequestService authorizationRequestService;
    private final OnboardStateContainer onboardStateContainer;
    private final SecuredOnboardProcessIntegrationService securedOnboardProcessIntegrationService;
    private final ApplicationRepository applicationRepository;
    private final EndpointService endpointService;
    private final BusinessOperationLogService businessOperationLogService;
    private final Gson gson;

    @Value("${app.agrirouter.threading.fixed-thread-pool-size}")
    private int fixedThreadPoolSize;

    /**
     * Generate the authorization URL for the application.
     *
     * @param application The application.
     * @return The URL to authorize the application against the AR.
     */
    public String generateAuthorizationUrl(Application application, String externalEndpointId, String redirectUrl) {
        if (null == application.getApplicationType()) {
            throw new BusinessException(ErrorMessageFactory.applicationDoesNotSupportSecuredOnboarding());
        } else {
            final var parameters = new AuthorizationRequestParameters();
            parameters.setApplicationId(application.getApplicationId());
            parameters.setResponseType(SecuredOnboardingResponseType.ONBOARD);
            var applicationSettingsRedirectUrl = application.getApplicationSettings().getRedirectUrl();
            final String redirectUrlToUse;
            if (StringUtils.isNotBlank(redirectUrl)) {
                redirectUrlToUse = redirectUrl;
            } else {
                redirectUrlToUse = applicationSettingsRedirectUrl;
            }
            parameters.setState(onboardStateContainer.push(application.getInternalApplicationId(), externalEndpointId, application.getTenant().getTenantId(), redirectUrlToUse));
            return authorizationRequestService.getAuthorizationRequestURL(parameters);
        }
    }

    /**
     * Onboard process for common telemetry connections or farming software.
     *
     * @param onboardProcessParameters -
     */
    public void onboard(OnboardProcessParameters onboardProcessParameters) {
        try {
            final var optionalApplication = applicationRepository.findByInternalApplicationIdAndTenantTenantId(onboardProcessParameters.getInternalApplicationId(), onboardProcessParameters.getTenantId());
            if (optionalApplication.isPresent()) {
                final var application = optionalApplication.get();
                final var existingEndpoint = endpointService.existsByExternalEndpointId(onboardProcessParameters.getExternalEndpointId());
                if (existingEndpoint) {
                    log.debug("Updating existing endpoint, this was a onboard process for an existing endpoint.");
                    final var optionalEndpoint = endpointService.findByExternalEndpointId(onboardProcessParameters.getExternalEndpointId());
                    if (optionalEndpoint.isPresent()) {
                        var endpoint = optionalEndpoint.get();
                        if (!endpoint.getAgrirouterAccountId().equals(onboardProcessParameters.getAccountId())) {
                            throw new BusinessException(ErrorMessageFactory.switchingAccountsWhenReOnboardingIsNotAllowed());
                        } else {

                            final var securedOnboardProcessIntegrationParameters = new SecuredOnboardProcessIntegrationParameters(application.getApplicationId(),
                                    application.getVersionId(),
                                    endpoint.getExternalEndpointId(),
                                    onboardProcessParameters.getRegistrationCode(),
                                    application.getPrivateKey(),
                                    application.getPublicKey());
                            final var onboardingResponse = securedOnboardProcessIntegrationService.onboard(securedOnboardProcessIntegrationParameters);
                            log.debug("Since this is an existing endpoint we need to modify the ID given by the AR.");
                            try {
                                var executorService = Executors.newFixedThreadPool(fixedThreadPoolSize);
                                updateExistingEndpoint(executorService, onboardProcessParameters, endpoint, onboardingResponse, application);
                            } catch (Exception e) {
                                log.error("Error while updating existing endpoint: {}", e.getMessage());
                            }
                        }
                    }
                } else {
                    final var securedOnboardProcessIntegrationParameters = new SecuredOnboardProcessIntegrationParameters(application.getApplicationId(),
                            application.getVersionId(),
                            onboardProcessParameters.getExternalEndpointId(),
                            onboardProcessParameters.getRegistrationCode(),
                            application.getPrivateKey(),
                            application.getPublicKey());
                    final var onboardingResponse = securedOnboardProcessIntegrationService.onboard(securedOnboardProcessIntegrationParameters);
                    log.debug("Create a new endpoint, since the endpoint does not exist in the database.");
                    try {
                        var executorService = Executors.newFixedThreadPool(fixedThreadPoolSize);
                        createNewEndpoint(executorService, onboardProcessParameters, onboardingResponse, securedOnboardProcessIntegrationParameters, application);
                    } catch (Exception e) {
                        log.error("Error while creating new endpoint: {}", e.getMessage());
                    }
                }
            } else {
                throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
            }
        } catch (OnboardingException e) {
            log.error("[{}] {}", ErrorMessageFactory.onboardRequestFailed().key(), ErrorMessageFactory.onboardRequestFailed().message());
            throw new BusinessException(ErrorMessageFactory.onboardRequestFailed(), e);
        }
    }

    private void createNewEndpoint(ExecutorService executorService, OnboardProcessParameters onboardProcessParameters, OnboardingResponse onboardingResponse, SecuredOnboardProcessIntegrationParameters securedOnboardProcessIntegrationParameters, Application application) {
        executorService.execute(() -> {
            final var endpoint = new Endpoint();
            endpoint.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
            endpoint.setExternalEndpointId(securedOnboardProcessIntegrationParameters.externalEndpointId());
            endpoint.setAgrirouterAccountId(onboardProcessParameters.getAccountId());
            endpoint.setOnboardResponse(gson.toJson(onboardingResponse));
            endpoint.setOnboardResponseForRouterDevice(application.createOnboardResponseForRouterDevice(endpoint.asOnboardingResponse(true)));
            endpointService.save(endpoint);
            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Endpoint was created.");
            application.getEndpoints().add(endpoint);
            applicationRepository.save(application);
            businessOperationLogService.log(new ApplicationLogInformation(application.getInternalApplicationId(), application.getApplicationId()), "The endpoint was added to the application.");
            endpointService.sendCapabilities(application, endpoint);
        });
    }

    private void updateExistingEndpoint(ExecutorService executorService, OnboardProcessParameters onboardProcessParameters, Endpoint endpoint, OnboardingResponse onboardingResponse, Application application) {
        executorService.execute(() -> {
            endpoint.setOnboardResponse(gson.toJson(onboardingResponse));
            endpoint.setOnboardResponseForRouterDevice(application.createOnboardResponseForRouterDevice(endpoint.asOnboardingResponse(true)));
            endpoint.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
            endpoint.setAgrirouterAccountId(onboardProcessParameters.getAccountId());
            endpoint.setDeactivated(false);
            endpointService.save(endpoint);
            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Endpoint was updated.");
            endpointService.sendCapabilities(application, endpoint);
        });
    }

}
