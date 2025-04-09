package de.agrirouter.middleware.business.dto.timelog.periods;

import java.util.List;

/**
 * All time log periods.
 *
 * @param timeLogPeriods All time log periods.
 */
public record TimeLogPeriods(List<TimeLogPeriod> timeLogPeriods) {

}
