package de.agrirouter.middleware.integration;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.CertificationType;
import com.dke.data.agrirouter.api.enums.Gateway;
import com.dke.data.agrirouter.api.service.onboard.secured.OnboardingService;
import com.dke.data.agrirouter.api.service.parameters.SecuredOnboardingParameters;
import de.agrirouter.middleware.integration.parameters.SecuredOnboardProcessIntegrationParameters;
import org.springframework.stereotype.Service;

/**
 * Integration service to handle the onboard requests.
 */
@Service
public class SecuredOnboardProcessIntegrationService {

    private final OnboardingService onboardingService;

    public SecuredOnboardProcessIntegrationService(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    /**
     * Secured onboard process.
     *
     * @param securedOnboardProcessIntegrationParameters -
     * @return -
     */
    public OnboardingResponse onboard(SecuredOnboardProcessIntegrationParameters securedOnboardProcessIntegrationParameters) {
        final var parameters = new SecuredOnboardingParameters();
        parameters.setUuid(securedOnboardProcessIntegrationParameters.externalEndpointId());
        parameters.setApplicationId(securedOnboardProcessIntegrationParameters.applicationId());
        parameters.setCertificationVersionId(securedOnboardProcessIntegrationParameters.versionId());
        parameters.setGatewayId(Gateway.MQTT.getKey());
        parameters.setCertificationType(CertificationType.P12);
        parameters.setRegistrationCode(securedOnboardProcessIntegrationParameters.registrationCode());
        parameters.setPrivateKey(securedOnboardProcessIntegrationParameters.privateKey());
        parameters.setPublicKey(securedOnboardProcessIntegrationParameters.publicKey());
        onboardingService.verify(parameters);
        return onboardingService.onboard(parameters);
    }

}
