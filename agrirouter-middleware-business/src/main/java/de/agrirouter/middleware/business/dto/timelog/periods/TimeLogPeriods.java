package de.agrirouter.middleware.business.dto.timelog.periods;

import lombok.Getter;

import java.util.List;

/**
 * All time log periods.
 */
@Getter
public class TimeLogPeriods {

    /**
     * All time log periods.
     */
    private final List<TimeLogPeriod> timeLogPeriods;

    public TimeLogPeriods(List<TimeLogPeriod> timeLogPeriods) {
        this.timeLogPeriods = timeLogPeriods;
    }
}
