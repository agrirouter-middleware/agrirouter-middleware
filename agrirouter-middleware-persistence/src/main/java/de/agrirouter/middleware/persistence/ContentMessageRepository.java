package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.ContentMessage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository to access the content messages.
 */
public interface ContentMessageRepository extends JpaRepository<ContentMessage, Long> {

    /**
     * Remove all content messages by the agrirouter© endpoint id.
     *
     * @param agrirouterEndpointId -
     */
    void deleteAllByAgrirouterEndpointId(String agrirouterEndpointId);
}
