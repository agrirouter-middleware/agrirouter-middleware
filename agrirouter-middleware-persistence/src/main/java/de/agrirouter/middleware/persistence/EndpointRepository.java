package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.enums.EndpointType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository to register endpoints from the agrirouter.
 */
@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, Long> {

    /**
     * Finding an endpoint by the given agrirouter© endpoint ID.
     *
     * @param agrirouterEndpointId The ID of the endpoint.
     * @return -
     */
    @Query("from Endpoint e where e.agrirouterEndpointId = :agrirouterEndpointId")
    Optional<Endpoint> findByAgrirouterEndpointId(@Param("agrirouterEndpointId") String agrirouterEndpointId);

    /**
     * Finding endpoint by the given endpoint ID .
     *
     * @param externalEndpointId The ID of the endpoint.
     * @return -
     */
    Optional<Endpoint> findByExternalEndpointId(String externalEndpointId);

    /**
     * Checks whether an endpoint with the given external endpoint ID exists.
     *
     * @param externalEndpointId The external endpoint ID.
     * @return True if the endpoint exists.
     */
    boolean existsByExternalEndpointId(String externalEndpointId);

    /**
     * Finding endpoints by the given internal application ID.
     *
     * @param internalApplicationId The internal ID of the application.
     * @return The endpoints.
     */
    @Query("select a.endpoints from Application a where a.internalApplicationId = :internalApplicationId")
    List<Endpoint> findAllByInternalApplicationId(String internalApplicationId);

    /**
     * Find all endpoints by the given external endpoint ID.
     *
     * @return The endpoints.
     */
    List<Endpoint> findAllByExternalEndpointId(String externalEndpointId);

    /**
     * Count all endpoints by the given endpoint type.
     *
     * @param endpointType The endpoint type.
     * @return The number of endpoints.
     */
    long countByEndpointType(EndpointType endpointType);

}
