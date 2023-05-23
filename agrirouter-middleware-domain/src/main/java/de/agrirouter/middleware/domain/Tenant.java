package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 * A tenant for the application.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class Tenant extends BaseEntity {

    /**
     * Internal ID of the tenant.
     */
    @Column(unique = true)
    private String tenantId;

    /**
     * Name of the tenant.
     */
    @Column(unique = true)
    private String name;

    /**
     * Access token for API usage.
     */
    private String accessToken;

    /**
     * The applications for the tenant.
     */
    @OneToMany(
            mappedBy = "tenant",
            cascade = CascadeType.ALL
    )
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
