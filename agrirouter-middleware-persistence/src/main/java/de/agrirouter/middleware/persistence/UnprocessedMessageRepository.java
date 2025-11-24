package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.UnprocessedMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to access all unprocessed messages.
 */
@Repository
public interface UnprocessedMessageRepository extends MongoRepository<UnprocessedMessage, String> {

    /**
     * Delete all unprocessed messages by the agrirouter© endpoint ID.
     *
     * @param agrirouterEndpointId -
     */
    void deleteAllByAgrirouterEndpointId(String agrirouterEndpointId);
}
