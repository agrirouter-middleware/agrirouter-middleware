package de.agrirouter.middleware.business.security;

import de.agrirouter.middleware.persistence.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;

/**
 * Service for authorization.
 */
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final ApplicationRepository applicationRepository;

    /**
     * Check if the principal is authorized to access the given application.
     *
     * @param principal             - Principal to check.
     * @param internalApplicationId - Application ID to check.
     * @return True if the principal is authorized to access the given application.
     */
    public boolean isAuthorized(Principal principal, String internalApplicationId) {
        return applicationRepository.findByInternalApplicationIdAndTenantId(internalApplicationId, principal.getName()).isPresent();
    }

}
