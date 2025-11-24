package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

/**
 * A tenant for the application.
 */
@Data
@Document
@ToString
@EqualsAndHashCode(callSuper = true)
public class Tenant extends BaseEntity {

    /**
     * Internal ID of the tenant.
     */
    @Indexed(unique = true)
    private String tenantId;

    /**
     * Name of the tenant.
     */
    @Indexed(unique = true)
    private String name;

    /**
     * Access token for API usage.
     */
    private String accessToken;

    /**
     * The applications for the tenant.
     */
    @ToString.Exclude
    private Set<Application> applications;

    /**
     * Marker if this tenant is a generated tenant.
     */
    private boolean generatedTenant = false;

    /**
     * Marker if this tenant has access to the monitoring.
     */
    private boolean monitoringAccess = false;

    /**
     * Marker if this tenant has access to the monitoring.
     */
    private boolean defaultTenant = false;
}
