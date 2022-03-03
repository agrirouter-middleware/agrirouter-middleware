package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.enums.SecuredOnboardingResponseType;
import com.dke.data.agrirouter.api.exception.OnboardingException;
import com.dke.data.agrirouter.api.service.onboard.secured.AuthorizationRequestService;
import com.dke.data.agrirouter.api.service.parameters.AuthorizationRequestParameters;
import com.google.gson.Gson;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.EndpointStatusUpdateEvent;
import de.agrirouter.middleware.business.global.OnboardStateContainer;
import de.agrirouter.middleware.business.parameters.OnboardProcessParameters;
import de.agrirouter.middleware.businesslog.BusinessLogService;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.SecuredOnboardProcessIntegrationService;
import de.agrirouter.middleware.integration.parameters.SecuredOnboardProcessIntegrationParameters;
import de.agrirouter.middleware.persistence.ApplicationRepository;
import de.agrirouter.middleware.persistence.EndpointRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * The service for the onboard process.
 */
@Service
public class SecuredOnboardProcessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecuredOnboardProcessService.class);

    private final AuthorizationRequestService authorizationRequestService;
    private final OnboardStateContainer onboardStateContainer;
    private final EndpointRepository endpointRepository;
    private final SecuredOnboardProcessIntegrationService securedOnboardProcessIntegrationService;
    private final ApplicationRepository applicationRepository;
    private final BusinessLogService businessLogService;
    private final EndpointService endpointService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SecuredOnboardProcessService(AuthorizationRequestService authorizationRequestService,
                                        OnboardStateContainer onboardStateContainer,
                                        EndpointRepository endpointRepository,
                                        SecuredOnboardProcessIntegrationService securedOnboardProcessIntegrationService,
                                        ApplicationRepository applicationRepository,
                                        BusinessLogService businessLogService,
                                        EndpointService endpointService,
                                        ApplicationEventPublisher applicationEventPublisher) {
        this.authorizationRequestService = authorizationRequestService;
        this.onboardStateContainer = onboardStateContainer;
        this.endpointRepository = endpointRepository;
        this.securedOnboardProcessIntegrationService = securedOnboardProcessIntegrationService;
        this.applicationRepository = applicationRepository;
        this.businessLogService = businessLogService;
        this.endpointService = endpointService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Generate the authorization URL for the application.
     *
     * @param application The application.
     * @return The URL to authorize the application against the AR.
     */
    public String generateAuthorizationUrl(Application application, String externalEndpointId, String redirectUrl) {
        if (null == application.getApplicationType() || application.getApplicationType().equals(de.agrirouter.middleware.domain.enums.ApplicationType.COMMUNICATION_UNIT)) {
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
                final var existingEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDisabled(onboardProcessParameters.getExternalEndpointId());
                final var application = optionalApplication.get();
                if (existingEndpoint.isPresent()) {
                    LOGGER.debug("Updating existing endpoint, this was a onboard process for an existing endpoint.");
                    final var endpoint = existingEndpoint.get();

                    if (!endpoint.getAgrirouterAccountId().equals(onboardProcessParameters.getAccountId())) {
                        throw new BusinessException(ErrorMessageFactory.switchingAccountsWhenReonboardingIsNotAllowed());
                    } else {

                        final var securedOnboardProcessIntegrationParameters = new SecuredOnboardProcessIntegrationParameters();
                        securedOnboardProcessIntegrationParameters.setExternalEndpointId(endpoint.getExternalEndpointId());
                        securedOnboardProcessIntegrationParameters.setApplicationId(application.getApplicationId());
                        securedOnboardProcessIntegrationParameters.setVersionId(application.getVersionId());
                        securedOnboardProcessIntegrationParameters.setRegistrationCode(onboardProcessParameters.getRegistrationCode());
                        securedOnboardProcessIntegrationParameters.setPrivateKey(application.getPrivateKey());
                        securedOnboardProcessIntegrationParameters.setPublicKey(application.getPublicKey());
                        final var onboardingResponse = securedOnboardProcessIntegrationService.onboard(securedOnboardProcessIntegrationParameters);

                        endpoint.setOnboardResponse(new Gson().toJson(onboardingResponse));
                        endpoint.setOnboardResponseForRouterDevice(application.createOnboardResponseForRouterDevice(endpoint.asOnboardingResponse(true)));

                        LOGGER.debug("Since this is an existing endpoint we need to modify the ID given by the AR.");
                        endpoint.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
                        endpoint.setAgrirouterAccountId(onboardProcessParameters.getAccountId());
                        endpointRepository.save(endpoint);
                        businessLogService.onboardEndpointAgain(endpoint);
                        endpointService.sendCapabilities(application, endpoint);
                        applicationEventPublisher.publishEvent(new EndpointStatusUpdateEvent(this, endpoint.getAgrirouterEndpointId(), null));
                    }
                } else {
                    LOGGER.debug("Create a new endpoint, since the endpoint does not exist in the database.");
                    final var securedOnboardProcessIntegrationParameters = new SecuredOnboardProcessIntegrationParameters();
                    securedOnboardProcessIntegrationParameters.setExternalEndpointId(onboardProcessParameters.getExternalEndpointId());
                    securedOnboardProcessIntegrationParameters.setApplicationId(application.getApplicationId());
                    securedOnboardProcessIntegrationParameters.setVersionId(application.getVersionId());
                    securedOnboardProcessIntegrationParameters.setRegistrationCode(onboardProcessParameters.getRegistrationCode());
                    securedOnboardProcessIntegrationParameters.setPrivateKey(application.getPrivateKey());
                    securedOnboardProcessIntegrationParameters.setPublicKey(application.getPublicKey());
                    final var onboardingResponse = securedOnboardProcessIntegrationService.onboard(securedOnboardProcessIntegrationParameters);

                    final var endpoint = new Endpoint();
                    endpoint.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
                    endpoint.setExternalEndpointId(securedOnboardProcessIntegrationParameters.getExternalEndpointId());
                    endpoint.setAgrirouterAccountId(onboardProcessParameters.getAccountId());
                    endpoint.setOnboardResponse(new Gson().toJson(onboardingResponse));
                    endpoint.setOnboardResponseForRouterDevice(application.createOnboardResponseForRouterDevice(endpoint.asOnboardingResponse(true)));
                    endpointRepository.save(endpoint);
                    application.getEndpoints().add(endpoint);
                    applicationRepository.save(application);
                    businessLogService.onboardEndpoint(endpoint);
                    endpointService.sendCapabilities(application, endpoint);
                    applicationEventPublisher.publishEvent(new EndpointStatusUpdateEvent(this, endpoint.getAgrirouterEndpointId(), null));
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
