package de.agrirouter.middleware.business.dto.timelog.periods;

/**
 * All time log periods for a team set.
 *
 * @param teamSetContextId The team set context ID.
 * @param timeLogPeriods   All time log periods.
 */
public record TimeLogPeriodsForTeamSet(String teamSetContextId, TimeLogPeriods timeLogPeriods) {

}
