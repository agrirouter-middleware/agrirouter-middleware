package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Access the registered tenants.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    /**
     * Search for a tenant by name.
     *
     * @param name Name to search for.
     * @return Tenant, if available.
     */
    Optional<Tenant> findTenantByNameIgnoreCase(String name);

    /**
     * Search for a tenant by tenant ID.
     *
     * @param tenantId ID to search for.
     * @return Tenant, if available.
     */
    Optional<Tenant> findTenantByTenantId(String tenantId);

    /**
     * Search for the default tenant.
     *
     * @return The default tenant, if available.
     */
    Optional<Tenant> findByDefaultTenantIsTrue();

    /**
     * Search for the generated tenant.
     *
     * @return The generated tenant, if available.
     */
    Optional<Tenant> findByMonitoringAccessIsTrue();

}
