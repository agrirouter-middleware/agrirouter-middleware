package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.TimeLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

/**
 * Repository to access the time logs within the MongoDB.
 */
public interface TimeLogRepository extends MongoRepository<TimeLog, String> {

    /**
     * Delete all time logs by agrirouter ID.
     *
     * @param agrirouterEndpointId -
     */
    void deleteAllByAgrirouterEndpointId(String agrirouterEndpointId);

    /**
     * Fetch all time logs for the dedicated team set context ID.
     *
     * @param teamSetContextId -
     */
    List<TimeLog> findAllByTeamSetContextId(String teamSetContextId);

    /**
     * Fetch all time logs for the dedicated team set context ID.
     *
     * @param teamSetContextId -
     * @param searchFrom       -
     * @param searchTo         -
     */
    List<TimeLog> findAllByTeamSetContextIdAndTimestampBetween(String teamSetContextId, long searchFrom, long searchTo);

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
