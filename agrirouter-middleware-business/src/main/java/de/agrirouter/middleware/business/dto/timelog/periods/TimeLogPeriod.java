package de.agrirouter.middleware.business.dto.timelog.periods;

import java.util.Set;

/**
 * A dedicated time log period.
 *
 * @param begin        Begin of the period.
 * @param end          End of the period.
 * @param nrOfTimeLogs Number Time logs within a period.
 * @param messageIds   The message IDs for this period.
 */
public record TimeLogPeriod(long begin, long end, int nrOfTimeLogs, Set<String> messageIds) {

}
