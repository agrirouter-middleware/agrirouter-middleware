package de.agrirouter.middleware.persistence.jpa;

import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository to access the applications within the database.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Fetch all applications for the tenant.
     *
     * @param tenantId The internal ID of the tenant.
     * @return -
     */
    List<Application> findAllByTenantTenantId(String tenantId);

    /**
     * Find an application by its internal ID and the belonging tenant.
     *
     * @param internalApplicationId The internal ID of the application.
     * @return -
     */
    Optional<Application> findByInternalApplicationIdAndTenantTenantId(String internalApplicationId, String tenantId);

    /**
     * Find an application by its internal ID.
     *
     * @param internalApplicationId The internal ID of the application.
     * @return -
     */
    Optional<Application> findByInternalApplicationId(String internalApplicationId);

    /**
     * Find an application by one of its onboard responses.
     *
     * @param endpoint The endpoint.
     * @return -
     */
    Optional<Application> findByEndpointsContains(Endpoint endpoint);

    /**
     * Find an application by its application ID.
     *
     * @param applicationId -
     * @return -
     */
    Optional<Application> findByApplicationIdAndVersionId(String applicationId, String versionId);
}
