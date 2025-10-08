package de.agrirouter.middleware.persistence.mongo;

import de.agrirouter.middleware.domain.documents.Farm;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to access the farms.
 */
@Repository
public interface FarmRepository extends MongoRepository<Farm, String> {
}
