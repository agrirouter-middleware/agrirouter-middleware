package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.TimeLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Set;

/**
 * Repository to access the time logs within the MongoDB.
 */
public interface TimeLogRepository extends MongoRepository<TimeLog, String> {

    /**
     * Delete all time logs by agrirouter© ID.
     *
     * @param agrirouterEndpointId -
     */
    void deleteAllByAgrirouterEndpointId(String agrirouterEndpointId);

    /**
     * Fetch all time logs for the dedicated team set context ID.
     *
     * @param teamSetContextId -
     */
    @Query(value = "{ 'teamSetContextId' : ?0 }", fields = "{ 'messageId' : 1, 'timestamp' : 1, 'teamSetContextId' : 1 }")
    List<TimeLog> findForTeamSetContextId(String teamSetContextId);

    /**
     * Fetch all time logs for the dedicated team set context ID.
     *
     * @param teamSetContextId -
     * @param searchFrom       -
     * @param searchTo         -
     */
    @Query(value = "{'teamSetContextId' : ?0, 'timestamp' : {'$gt' : ?1, '$lt' : ?2}}", fields = "{ 'messageId' : 1, 'timestamp' : 1, 'teamSetContextId' : 1 }")
    List<TimeLog> findForTeamSetContextIdAndTimestampBetween(String teamSetContextId, long searchFrom, long searchTo);

    /**
     * Fetch all time logs that have the dedicated message ID.
     *
     * @param messageIds -
     */
    List<TimeLog> findAllByMessageIdIn(Set<String> messageIds);

    /**
     * Fetch all time logs that have the dedicated message ID.
     *
     * @param messageIds -
     * @param searchFrom -
     * @param searchTo   -
     */
    List<TimeLog> findAllByMessageIdInAndTimestampBetween(Set<String> messageIds, long searchFrom, long searchTo);

    /**
     * Fetch all time logs that have the dedicated message ID.
     *
     * @param searchFrom -
     * @param searchTo   -
     */
    List<TimeLog> findAllByTimestampBetween(long searchFrom, long searchTo);

}
