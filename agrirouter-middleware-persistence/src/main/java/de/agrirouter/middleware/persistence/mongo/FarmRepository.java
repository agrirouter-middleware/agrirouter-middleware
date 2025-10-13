package de.agrirouter.middleware.persistence.mongo;

import de.agrirouter.middleware.domain.documents.Farm;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    /**
     * Find a farm by its external endpoint ID and farm ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param farmId             The farm ID.
     * @return The farm.
     */
    Optional<Farm> findByExternalEndpointIdAndDocument_farmId(String externalEndpointId, String farmId);
}
