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
     * Finding an endpoint by the given endpoint ID.
     *
     * @param externalEndpointId The ID of the endpoint.
     * @return -
     */
    @Query("from Endpoint e where lower(e.externalEndpointId) = lower(:externalEndpointId) and e.deactivated = false")
    Optional<Endpoint> findByExternalEndpointIdAndIgnoreDeactivated(@Param("externalEndpointId") String externalEndpointId);

    /**
     * Finding endpoint by the given endpoint ID and a specific type.
     *
     * @param endpointId The ID of the endpoint.
     * @return -
     */
    @Query("from Endpoint e where e.externalEndpointId = :endpointId and e.endpointType = :endpointType and e.deactivated = false")
    Optional<Endpoint> findByExternalEndpointIdAndEndpointType(@Param("endpointId") String endpointId, @Param("endpointType") EndpointType endpointType);

    /**
     * Finding endpoint by the given endpoint ID and a specific type.
     *
     * @param endpointId The ID of the endpoint.
     * @return -
     */
    @Query("from Endpoint e where e.externalEndpointId = :endpointId and e.endpointType = :endpointType")
    Optional<Endpoint> findAllByExternalEndpointIdAndEndpointType(@Param("endpointId") String endpointId, @Param("endpointType") EndpointType endpointType);

    /**
     * Finding endpoint by the given endpoint ID .
     *
     * @param endpointId The ID of the endpoint.
     * @return -
     */
    Optional<Endpoint> findByExternalEndpointId(String endpointId);

    /**
     * Finding endpoints by the given endpoint IDs .
     *
     * @param endpointIds The IDs of the endpoints.
     * @return -
     */
    List<Endpoint> findByExternalEndpointIdIsIn(List<String> endpointIds);

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
