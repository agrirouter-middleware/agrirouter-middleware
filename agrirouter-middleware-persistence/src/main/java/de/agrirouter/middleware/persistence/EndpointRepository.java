package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.Endpoint;
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
    @Query("from Endpoint e where e.agrirouterEndpointId = :agrirouterEndpointId and e.deactivated = false")
    Optional<Endpoint> findByAgrirouterEndpointId(@Param("agrirouterEndpointId") String agrirouterEndpointId);

    /**
     * Finding an endpoint by the given agrirouter© endpoint ID and ignore deactivated.
     *
     * @param agrirouterEndpointId The ID of the endpoint.
     * @return -
     */
    @Query("from Endpoint e where e.agrirouterEndpointId = :agrirouterEndpointId")
    Optional<Endpoint> findByAgrirouterEndpointIdAndIgnoreDeactivated(@Param("agrirouterEndpointId") String agrirouterEndpointId);

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
     * Checks whether an endpoint with the given agrirouter endpoint ID exists.
     *
     * @param agrirouterEndpointId The agrirouter endpoint ID.
     * @return True if the endpoint exists.
     */
    boolean existsByAgrirouterEndpointId(String agrirouterEndpointId);

    /**
     * Finding endpoints by the given internal application ID.
     *
     * @param internalApplicationId The internal ID of the application.
     * @return The endpoints.
     */
    @Query("select a.endpoints from Application a where a.internalApplicationId = :internalApplicationId")
    List<Endpoint> findAllByInternalApplicationId(String internalApplicationId);

    /**
     * Delete the endpoint by its internal endpoint ID.
     *
     * @param externalEndpointId The external endpoint ID.
     */
    void deleteByExternalEndpointId(String externalEndpointId);

    /**
     * Find all endpoints by the given external endpoint ID.
     *
     * @return The endpoints.
     */
    List<Endpoint> findAllByExternalEndpointId(String externalEndpointId);
}
