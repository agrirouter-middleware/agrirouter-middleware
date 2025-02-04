package de.agrirouter.middleware.persistence.jpa;

import de.agrirouter.middleware.domain.UnprocessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to access all unprocessed messages.
 */
@Repository
public interface UnprocessedMessageRepository extends JpaRepository<UnprocessedMessage, Long> {

    /**
     * Delete all unprocessed messages by the agrirouter© endpoint ID.
     *
     * @param agrirouterEndpointId -
     */
    void deleteAllByAgrirouterEndpointId(String agrirouterEndpointId);
}
