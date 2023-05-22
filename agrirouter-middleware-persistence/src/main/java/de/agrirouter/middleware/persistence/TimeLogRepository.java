package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.TimeLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

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
     * Fetch all time logs that are within the timestamp.
     *
     * @param searchFrom The start of the search interval.
     * @param searchTo   The end of the search interval.
     * @return The time logs.
     */
    List<TimeLog> findAllByTimestampBetween(long searchFrom, long searchTo);

    /**
     * Fetch all time logs are within the timestamp and have the given team set context ID.
     *
     * @param searchFrom       The start of the search interval.
     * @param searchTo         The end of the search interval.
     * @param teamSetContextId The team set context ID.
     * @return The time logs.
     */
    List<TimeLog> findAllByTimestampBetweenAndTeamSetContextIdEqualsIgnoreCase(long searchFrom, long searchTo, String teamSetContextId);

    /**
     * Find all by team set context ID.
     *
     * @param teamSetContextId The team set context ID.
     * @return The time logs.
     */
    List<TimeLog> findAllByTeamSetContextIdEqualsIgnoreCase(String teamSetContextId);

}
