package de.agrirouter.middleware.business.dto.timelog.periods;

import lombok.Getter;

/**
 * All time log periods for a team set.
 */
@Getter
public class TimeLogPeriodsForTeamSet {

    /**
     * The team set context ID.
     */
    private final String teamSetContextId;

    /**
     * All time log periods.
     */
    private final TimeLogPeriods timeLogPeriods;

    public TimeLogPeriodsForTeamSet(String teamSetContextId,
                                    TimeLogPeriods timeLogPeriods) {
        this.teamSetContextId = teamSetContextId;
        this.timeLogPeriods = timeLogPeriods;
    }
}
