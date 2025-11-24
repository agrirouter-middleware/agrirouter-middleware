package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.ContentMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository to access the content messages.
 */
@Repository
public interface ContentMessageRepository extends MongoRepository<ContentMessage, String> {

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
    @Query(value = "{ 'agrirouterEndpointId': ?0, 'contentMessageMetadata.technicalMessageType': { $in: ?1, $nin: ?2 }, 'contentMessageMetadata.timestamp': { $gte: ?3, $lte: ?4 } }", 
           fields = "{ 'contentMessageMetadata': 1 }")
    List<ContentMessage> findMetadataByFilters(String agrirouterEndpointId,
                                               List<String> technicalMessageTypes,
                                               List<String> technicalMessageTypesThatAreNotAllowed,
                                               long searchFrom,
                                               long searchTo);

    /**
     * Search for existing content messages.
     *
     * @param agrirouterEndpointId -
     * @param searchFrom           -
     * @param searchTo             -
     * @return -
     */
    @Query(value = "{ 'agrirouterEndpointId': ?0, 'contentMessageMetadata.technicalMessageType': { $nin: ?1 }, 'contentMessageMetadata.timestamp': { $gte: ?2, $lte: ?3 } }", 
           fields = "{ 'contentMessageMetadata': 1 }")
    List<ContentMessage> findMetadataByEndpointAndTimeRange(String agrirouterEndpointId,
                                               List<String> technicalMessageTypesThatAreNotAllowed,
                                               long searchFrom,
                                               long searchTo);

    /**
     * Count the number of messages for the given endpoint.
     *
     * @param agrirouterEndpointId The endpoint id.
     * @return The number of messages grouped by sender and technical message type.
     */
    List<ContentMessage> findAllByAgrirouterEndpointId(String agrirouterEndpointId);

}
