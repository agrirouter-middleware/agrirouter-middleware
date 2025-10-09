package de.agrirouter.middleware.persistence.mongo;

import de.agrirouter.middleware.domain.documents.Field;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to access the fields.
 */
@Repository
public interface FieldRepository extends MongoRepository<Field, String> {
}
