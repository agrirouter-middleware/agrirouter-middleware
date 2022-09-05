package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.enums.SecuredOnboardingResponseType;
import com.dke.data.agrirouter.api.exception.OnboardingException;
import com.dke.data.agrirouter.api.service.onboard.secured.AuthorizationRequestService;
import com.dke.data.agrirouter.api.service.parameters.AuthorizationRequestParameters;
import com.google.gson.Gson;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.EndpointStatusUpdateEvent;
import de.agrirouter.middleware.api.logging.ApplicationLogInformation;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.global.OnboardStateContainer;
import de.agrirouter.middleware.business.parameters.OnboardProcessParameters;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.SecuredOnboardProcessIntegrationService;
import de.agrirouter.middleware.integration.parameters.SecuredOnboardProcessIntegrationParameters;
import de.agrirouter.middleware.persistence.ApplicationRepository;
import de.agrirouter.middleware.persistence.EndpointRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * The service for the onboard process.
 */
@Slf4j
@Service
public class SecuredOnboardProcessService {

    private final AuthorizationRequestService authorizationRequestService;
    private final OnboardStateContainer onboardStateContainer;
    private final EndpointRepository endpointRepository;
    private final SecuredOnboardProcessIntegrationService securedOnboardProcessIntegrationService;
    private final ApplicationRepository applicationRepository;
    private final EndpointService endpointService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BusinessOperationLogService businessOperationLogService;

    public SecuredOnboardProcessService(AuthorizationRequestService authorizationRequestService,
                                        OnboardStateContainer onboardStateContainer,
                                        EndpointRepository endpointRepository,
                                        SecuredOnboardProcessIntegrationService securedOnboardProcessIntegrationService,
                                        ApplicationRepository applicationRepository,
                                        EndpointService endpointService,
                                        ApplicationEventPublisher applicationEventPublisher,
                                        BusinessOperationLogService businessOperationLogService) {
        this.authorizationRequestService = authorizationRequestService;
        this.onboardStateContainer = onboardStateContainer;
        this.endpointRepository = endpointRepository;
        this.securedOnboardProcessIntegrationService = securedOnboardProcessIntegrationService;
        this.applicationRepository = applicationRepository;
        this.endpointService = endpointService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.businessOperationLogService = businessOperationLogService;
    }

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
                final var existingEndpoint = endpointRepository.findByExternalEndpointId(onboardProcessParameters.getExternalEndpointId());
                final var application = optionalApplication.get();
                if (existingEndpoint.isPresent()) {
                    log.debug("Updating existing endpoint, this was a onboard process for an existing endpoint.");
                    final var endpoint = existingEndpoint.get();

                    if (!endpoint.getAgrirouterAccountId().equals(onboardProcessParameters.getAccountId())) {
                        throw new BusinessException(ErrorMessageFactory.switchingAccountsWhenReonboardingIsNotAllowed());
                    } else {

                        final var securedOnboardProcessIntegrationParameters = new SecuredOnboardProcessIntegrationParameters(application.getApplicationId(),
                                application.getVersionId(),
                                endpoint.getExternalEndpointId(),
                                onboardProcessParameters.getRegistrationCode(),
                                application.getPrivateKey(),
                                application.getPublicKey());
                        final var onboardingResponse = securedOnboardProcessIntegrationService.onboard(securedOnboardProcessIntegrationParameters);

                        endpoint.setOnboardResponse(new Gson().toJson(onboardingResponse));
                        endpoint.setOnboardResponseForRouterDevice(application.createOnboardResponseForRouterDevice(endpoint.asOnboardingResponse(true)));

                        log.debug("Since this is an existing endpoint we need to modify the ID given by the AR.");
                        endpoint.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
                        endpoint.setAgrirouterAccountId(onboardProcessParameters.getAccountId());
                        endpoint.setDeactivated(false);
                        endpointRepository.save(endpoint);
                        businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Endpoint was updated.");
                        endpointService.sendCapabilities(application, endpoint);
                        applicationEventPublisher.publishEvent(new EndpointStatusUpdateEvent(this, endpoint.getAgrirouterEndpointId(), null));
                    }
                } else {
                    log.debug("Create a new endpoint, since the endpoint does not exist in the database.");
                    final var securedOnboardProcessIntegrationParameters = new SecuredOnboardProcessIntegrationParameters(application.getApplicationId(),
                            application.getVersionId(),
                            onboardProcessParameters.getExternalEndpointId(),
                            onboardProcessParameters.getRegistrationCode(),
                            application.getPrivateKey(),
                            application.getPublicKey());
                    final var onboardingResponse = securedOnboardProcessIntegrationService.onboard(securedOnboardProcessIntegrationParameters);

                    final var endpoint = new Endpoint();
                    endpoint.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
                    endpoint.setExternalEndpointId(securedOnboardProcessIntegrationParameters.externalEndpointId());
                    endpoint.setAgrirouterAccountId(onboardProcessParameters.getAccountId());
                    endpoint.setOnboardResponse(new Gson().toJson(onboardingResponse));
                    endpoint.setOnboardResponseForRouterDevice(application.createOnboardResponseForRouterDevice(endpoint.asOnboardingResponse(true)));
                    endpointRepository.save(endpoint);
                    businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Endpoint was created.");
                    application.getEndpoints().add(endpoint);
                    applicationRepository.save(application);
                    businessOperationLogService.log(new ApplicationLogInformation(application.getInternalApplicationId(), application.getApplicationId()), "The endpoint was added to the application.");
                    endpointService.sendCapabilities(application, endpoint);
                    applicationEventPublisher.publishEvent(new EndpointStatusUpdateEvent(this, endpoint.getAgrirouterEndpointId(), null));
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
