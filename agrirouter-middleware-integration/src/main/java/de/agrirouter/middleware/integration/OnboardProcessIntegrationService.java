package de.agrirouter.middleware.integration;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.ApplicationType;
import com.dke.data.agrirouter.api.enums.CertificationType;
import com.dke.data.agrirouter.api.enums.Gateway;
import com.dke.data.agrirouter.api.service.onboard.OnboardingService;
import com.dke.data.agrirouter.api.service.parameters.OnboardingParameters;
import de.agrirouter.middleware.integration.parameters.OnboardProcessIntegrationParameters;
import org.springframework.stereotype.Service;

/**
 * Integration service to handle the onboard requests.
 */
@Service
public class OnboardProcessIntegrationService {

    private final OnboardingService onboardingService;

    public OnboardProcessIntegrationService(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    /**
     * Onboard a communication unit using MQTT for the communication with the AR.
     *
     * @param onboardProcessIntegrationParameters -
     * @return The container holding the onboard response from the AR.
     */
    public OnboardingResponse onboard(OnboardProcessIntegrationParameters onboardProcessIntegrationParameters) {
        final var parameters = new OnboardingParameters();
        parameters.setUuid(onboardProcessIntegrationParameters.endpointId());
        parameters.setApplicationId(onboardProcessIntegrationParameters.applicationId());
        parameters.setCertificationVersionId(onboardProcessIntegrationParameters.versionId());
        parameters.setApplicationType(ApplicationType.APPLICATION);
        parameters.setGatewayId(Gateway.MQTT.getKey());
        parameters.setCertificationType(CertificationType.P12);
        parameters.setRegistrationCode(onboardProcessIntegrationParameters.registrationCode());
        return onboardingService.onboard(parameters);
    }

}
