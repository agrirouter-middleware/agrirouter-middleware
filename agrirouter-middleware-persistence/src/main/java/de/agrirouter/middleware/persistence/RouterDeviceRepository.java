package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.RouterDevice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to access the router devices within the database.
 */
@Repository
public interface RouterDeviceRepository extends MongoRepository<RouterDevice, String> {

    /**
     * Check if a router device exists by the given client ID.
     *
     * @param id       the ID
     * @param clientId the client ID
     * @return true if the router device exists, otherwise false
     */
    boolean existsByIdNotAndConnectionCriteriaClientId(String id, String clientId);

}
