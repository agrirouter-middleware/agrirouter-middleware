package de.agrirouter.middleware.business.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Result of the tenant registration.
 */
@Setter
@Getter
@ToString
public class TenantRegistrationResult {

    /**
     * Internal ID of the tenant.
     */
    private String tenantId;

    /**
     * Access token for API usage.
     */
    private String accessToken;

}
