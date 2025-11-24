package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository to access the applications within the database.
 */
@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {

    /**
     * Fetch all applications for the tenant.
     *
     * @param tenantId The internal ID of the tenant.
     * @return -
     */
    List<Application> findAllByTenantId(String tenantId);

    /**
     * Find an application by its internal ID and the belonging tenant.
     *
     * @param internalApplicationId The internal ID of the application.
     * @return -
     */
    Optional<Application> findByInternalApplicationIdAndTenantId(String internalApplicationId, String tenantId);

    /**
     * Find an application by its internal ID.
     *
     * @param internalApplicationId The internal ID of the application.
     * @return -
     */
    Optional<Application> findByInternalApplicationId(String internalApplicationId);

    /**
     * Find an application by one of its endpoint IDs.
     *
     * @param endpointId The endpoint ID.
     * @return -
     */
    @Query("{ 'endpointIds': ?0 }")
    Optional<Application> findByEndpointIdsContains(String endpointId);

    /**
     * Find an application by its application ID.
     *
     * @param applicationId -
     * @return -
     */
    Optional<Application> findByApplicationIdAndVersionId(String applicationId, String versionId);
}
