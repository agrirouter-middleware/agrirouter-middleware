package de.agrirouter.middleware.persistence.mongo;

import de.agrirouter.middleware.domain.documents.Farm;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository to access the farms.
 */
@Repository
public interface FarmRepository extends MongoRepository<Farm, String> {

    /**
     * Find all farms for the given external endpoint ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @return The farms.
     */
    List<Farm> findByExternalEndpointId(String externalEndpointId);
}
