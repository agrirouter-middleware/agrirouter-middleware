package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.DeviceDescription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository to access the time logs within the MongoDB.
 */
public interface DeviceDescriptionRepository extends MongoRepository<DeviceDescription, String> {

    /**
     * Search for an existing device description by its ID.
     *
     * @param teamSetContextId -
     * @return -
     */
    Optional<DeviceDescription> findByTeamSetContextId(String teamSetContextId);

    /**
     * Delete all device descriptions by agrirouter© ID.
     *
     * @param agrirouterEndpointId -
     */
    void deleteAllByAgrirouterEndpointId(String agrirouterEndpointId);

}
