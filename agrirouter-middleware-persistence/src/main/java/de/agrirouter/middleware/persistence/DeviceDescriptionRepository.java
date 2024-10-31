package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.documents.DeviceDescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository to access the time logs within the MongoDB.
 */
@Repository
public interface DeviceDescriptionRepository extends MongoRepository<DeviceDescription, String> {

    /**
     * Search for an existing device description by its ID.
     *
     * @param teamSetContextId The ID of the device description to search for.
     * @return -
     */
    Optional<DeviceDescription> findByTeamSetContextId(String teamSetContextId);

    /**
     * Search for existing device descriptions by its ID.
     *
     * @param teamSetContextId The ID of the device description to search for.
     * @return -
     */
    Optional<DeviceDescription> findFirstByTeamSetContextIdOrderByTimestampDesc(String teamSetContextId);

    /**
     * Delete all device descriptions by agrirouter© ID.
     *
     * @param agrirouterEndpointId The agrirouter© endpoint ID.
     */
    void deleteAllByAgrirouterEndpointId(String agrirouterEndpointId);

}
