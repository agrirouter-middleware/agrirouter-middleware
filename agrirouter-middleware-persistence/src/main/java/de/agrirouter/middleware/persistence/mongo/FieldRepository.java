package de.agrirouter.middleware.persistence.mongo;

import de.agrirouter.middleware.domain.documents.Field;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository to access the fields.
 */
@Repository
public interface FieldRepository extends MongoRepository<Field, String> {

    /**
     * Find all fields for the given external endpoint ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @return The fields.
     */
    List<Field> findAllByExternalEndpointId(String externalEndpointId);

}
