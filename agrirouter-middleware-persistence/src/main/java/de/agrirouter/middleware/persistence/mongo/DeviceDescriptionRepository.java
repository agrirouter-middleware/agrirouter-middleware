package de.agrirouter.middleware.persistence.mongo;

import de.agrirouter.middleware.domain.documents.DeviceDescription;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * Return all distinct, non-blank team set context IDs present in the collection.
     * Uses an aggregation to avoid loading full documents into memory.
     *
     * @return list of projections exposing each distinct team set context ID.
     */
    @Aggregation(pipeline = {
            "{ '$match': { 'teamSetContextId': { '$exists': true, '$nin': [null, ''] } } }",
            "{ '$group': { '_id': '$teamSetContextId', 'teamSetContextId': { '$first': '$teamSetContextId' } } }"
    })
    List<TeamSetContextIdOnly> findDistinctTeamSetContextIds();

    /**
     * Find all device descriptions for the given team set context ID, newest first.
     *
     * @param teamSetContextId The team set context ID.
     * @return list of matching device descriptions ordered by timestamp descending.
     */
    List<DeviceDescription> findByTeamSetContextIdOrderByTimestampDesc(String teamSetContextId);

    /**
     * Projection for retrieving only the team set context ID from a device description.
     */
    interface TeamSetContextIdOnly {
        String getTeamSetContextId();
    }

}
