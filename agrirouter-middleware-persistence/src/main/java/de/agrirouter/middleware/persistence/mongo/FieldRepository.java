package de.agrirouter.middleware.persistence.mongo;

import de.agrirouter.middleware.domain.documents.Field;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    /**
     * Find a field by its external endpoint ID and field ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param fieldId            The field ID.
     * @return The field.
     */
    Optional<Field> findByExternalEndpointIdAndFieldId(String externalEndpointId, String fieldId);
}
