package de.agrirouter.middleware.business.security;

import de.agrirouter.middleware.persistence.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.security.Principal;

/**
 * Service for authorization.
 */
@Service
public class AuthorizationService {

    private final ApplicationRepository applicationRepository;

    public AuthorizationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    /**
     * Check if the principal is authorized to access the given application.
     *
     * @param principal             - Principal to check.
     * @param internalApplicationId - Application ID to check.
     * @return True if the principal is authorized to access the given application.
     */
    public boolean isAuthorized(Principal principal, String internalApplicationId) {
        return applicationRepository.findByInternalApplicationIdAndTenantTenantId(internalApplicationId, principal.getName()).isPresent();
    }

}
