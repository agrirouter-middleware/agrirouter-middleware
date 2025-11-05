package de.agrirouter.middleware.persistence.mongo;

import de.agrirouter.middleware.domain.documents.TimeLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository to access the time logs within the MongoDB.
 */
@Repository
public interface TimeLogRepository extends MongoRepository<TimeLog, String> {

    /**
     * Delete all time logs by agrirouter© ID.
     *
     * @param agrirouterEndpointId -
     */
    void deleteAllByAgrirouterEndpointId(String agrirouterEndpointId);

    /**
     * Fetch all time logs that are within the timestamp (inclusive bounds).
     *
     * @param searchFrom The start of the search interval (inclusive).
     * @param searchTo   The end of the search interval (inclusive).
     * @return The time logs.
     */
    List<TimeLog> findAllByTimestampGreaterThanEqualAndTimestampLessThanEqual(long searchFrom, long searchTo);

    /**
     * Fetch all time logs are within the timestamp and have the given team set context ID (inclusive bounds).
     *
     * @param searchFrom       The start of the search interval (inclusive).
     * @param searchTo         The end of the search interval (inclusive).
     * @param teamSetContextId The team set context ID.
     * @return The time logs.
     */
    List<TimeLog> findAllByTimestampGreaterThanEqualAndTimestampLessThanEqualAndTeamSetContextIdEqualsIgnoreCase(long searchFrom, long searchTo, String teamSetContextId);

    /**
     * Find all by team set context ID.
     *
     * @param teamSetContextId The team set context ID.
     * @return The time logs.
     */
    List<TimeLog> findAllByTeamSetContextIdEqualsIgnoreCase(String teamSetContextId);

}
