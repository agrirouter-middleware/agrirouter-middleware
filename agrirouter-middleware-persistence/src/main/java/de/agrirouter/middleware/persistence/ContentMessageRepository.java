package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.ContentMessageMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

    /**
     * Search for existing content messages.
     */
    @Query("select c.contentMessageMetadata from ContentMessage c " +
            "where c.agrirouterEndpointId = :agrirouterEndpointId " +
            "and c.contentMessageMetadata.technicalMessageType in :technicalMessageTypes " +
            "and c.contentMessageMetadata.timestamp >= :searchFrom " +
            "and c.contentMessageMetadata.timestamp <= :searchTo")
    List<ContentMessageMetadata> findMetadata(@Param("agrirouterEndpointId") String agrirouterEndpointId,
                                              @Param("technicalMessageTypes") List<String> technicalMessageTypes,
                                              @Param("searchFrom") long searchFrom,
                                              @Param("searchTo") long searchTo);

    /**
     * Search for existing content messages.
     */
    @Query("select c.contentMessageMetadata from ContentMessage c " +
            "where c.agrirouterEndpointId = :agrirouterEndpointId " +
            "and c.contentMessageMetadata.timestamp >= :searchFrom " +
            "and c.contentMessageMetadata.timestamp <= :searchTo")
    List<ContentMessageMetadata> findMetadata(@Param("agrirouterEndpointId") String agrirouterEndpointId,
                                              @Param("searchFrom") long searchFrom,
                                              @Param("searchTo") long searchTo);
}
