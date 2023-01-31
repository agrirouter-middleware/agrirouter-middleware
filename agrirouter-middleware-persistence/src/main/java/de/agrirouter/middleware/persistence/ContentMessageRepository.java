package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.ContentMessageMetadata;
import de.agrirouter.middleware.persistence.projections.MessageCountForTechnicalMessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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
     * Find a content message for the given endpoint.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @param messageId            The message ID.
     * @return -
     */
    Optional<ContentMessage> findFirstByAgrirouterEndpointIdAndContentMessageMetadataMessageId(String agrirouterEndpointId, String messageId);

    /**
     * Delete a content message for the given endpoint.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @param messageId            The message ID.
     */
    int deleteByAgrirouterEndpointIdAndContentMessageMetadataMessageId(String agrirouterEndpointId, String messageId);

    /**
     * Find all messages for the given endpoint and chunk context ID.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @param chunkContextId       The chunk context ID.
     * @return -
     */
    List<ContentMessage> findByAgrirouterEndpointIdAndContentMessageMetadataChunkContextId(String agrirouterEndpointId, String chunkContextId);

    /**
     * Find all messages for the given endpoint and chunk context ID.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @param chunkContextId       The chunk context ID.
     * @return -
     */
    int deleteByAgrirouterEndpointIdAndContentMessageMetadataChunkContextId(String agrirouterEndpointId, String chunkContextId);

    /**
     * Search for existing content messages.
     *
     * @param agrirouterEndpointId  The endpoint ID.
     * @param technicalMessageTypes The technical message types.
     * @param searchFrom            The search from.
     * @param searchTo              The search to.
     * @return -
     */
    @Query("select c.contentMessageMetadata from ContentMessage c " +
            "where c.agrirouterEndpointId = :agrirouterEndpointId " +
            "and c.contentMessageMetadata.technicalMessageType in :technicalMessageTypes " +
            "and c.contentMessageMetadata.technicalMessageType not in :technicalMessageTypesThatAreNotAllowed " +
            "and c.contentMessageMetadata.timestamp >= :searchFrom " +
            "and c.contentMessageMetadata.timestamp <= :searchTo")
    List<ContentMessageMetadata> findMetadata(@Param("agrirouterEndpointId") String agrirouterEndpointId,
                                              @Param("technicalMessageTypes") List<String> technicalMessageTypes,
                                              @Param("technicalMessageTypesThatAreNotAllowed") List<String> technicalMessageTypesThatAreNotAllowed,
                                              @Param("searchFrom") long searchFrom,
                                              @Param("searchTo") long searchTo);

    /**
     * Search for existing content messages.
     *
     * @param agrirouterEndpointId -
     * @param searchFrom           -
     * @param searchTo             -
     * @return -
     */
    @Query("select c.contentMessageMetadata from ContentMessage c " +
            "where c.agrirouterEndpointId = :agrirouterEndpointId " +
            "and c.contentMessageMetadata.technicalMessageType not in :technicalMessageTypesThatAreNotAllowed " +
            "and c.contentMessageMetadata.timestamp >= :searchFrom " +
            "and c.contentMessageMetadata.timestamp <= :searchTo")
    List<ContentMessageMetadata> findMetadata(@Param("agrirouterEndpointId") String agrirouterEndpointId,
                                              @Param("technicalMessageTypesThatAreNotAllowed") List<String> technicalMessageTypesThatAreNotAllowed,
                                              @Param("searchFrom") long searchFrom,
                                              @Param("searchTo") long searchTo);

    /**
     * Count the number of messages for the given endpoint.
     *
     * @param agrirouterEndpointId The endpoint id.
     * @return The number of messages.
     */
    @Query("select new de.agrirouter.middleware.persistence.projections.MessageCountForTechnicalMessageType(c.contentMessageMetadata.senderId, c.contentMessageMetadata.technicalMessageType, count(c.contentMessageMetadata)) from ContentMessage c " +
            " where c.agrirouterEndpointId = :agrirouterEndpointId " +
            "group by c.contentMessageMetadata.senderId, c.contentMessageMetadata.technicalMessageType")
    List<MessageCountForTechnicalMessageType> countMessagesGroupedByTechnicalMessageType(@Param("agrirouterEndpointId") String agrirouterEndpointId);

}
