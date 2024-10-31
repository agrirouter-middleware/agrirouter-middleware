package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.documents.MessageCacheEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to access the message cache entries within the MongoDB.
 */
@Repository
public interface MessageCacheEntryRepository extends MongoRepository<MessageCacheEntry, String> {

    /**
     * Counts the number of message cache entries associated with the given external endpoint ID.
     *
     * @param externalEndpointId the external endpoint ID to filter message cache entries by.
     * @return the count of message cache entries that match the given external endpoint ID.
     */
    long countAllByExternalEndpointId(String externalEndpointId);
}
