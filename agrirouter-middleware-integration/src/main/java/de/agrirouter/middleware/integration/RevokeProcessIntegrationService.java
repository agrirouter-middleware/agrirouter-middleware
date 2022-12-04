package de.agrirouter.middleware.integration;

import com.dke.data.agrirouter.api.exception.RevokingException;
import com.dke.data.agrirouter.api.service.RevokingService;
import com.dke.data.agrirouter.api.service.parameters.RevokeParameters;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service for the offboard process for existing endpoints.
 */
@Slf4j
@Service
public class RevokeProcessIntegrationService {

    private final RevokingService revokingService;

    public RevokeProcessIntegrationService(RevokingService revokingService) {
        this.revokingService = revokingService;
    }

    /**
     * Revoke an existing endpoint.
     *
     * @param application -
     * @param endpoint    -
     */
    public void revoke(Application application, Endpoint endpoint) {
        try {
            final var revokeParameters = new RevokeParameters();
            revokeParameters.setApplicationId(application.getApplicationId());
            revokeParameters.setPublicKey(application.getPublicKey());
            revokeParameters.setPrivateKey(application.getPrivateKey());
            revokeParameters.setAccountId(endpoint.getAgrirouterAccountId());
            revokeParameters.setEndpointIds(Collections.singletonList(endpoint.asOnboardingResponse().getSensorAlternateId()));
            revokingService.revoke(revokeParameters);
        } catch (RevokingException e) {
            log.error("Could not revoke existing endpoint from the AR. The data is removed from the middleware.", e);
        }
    }

}
