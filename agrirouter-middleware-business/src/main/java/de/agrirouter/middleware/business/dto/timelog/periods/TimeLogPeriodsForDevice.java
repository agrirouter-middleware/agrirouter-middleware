package de.agrirouter.middleware.business.dto.timelog.periods;

import de.agrirouter.middleware.domain.documents.Device;

import java.util.List;

/**
 * All time log periods for a team set.
 *
 * @param device                   The device.
 * @param timeLogPeriodsForTeamSet All time log periods for the belonging team sets.
 */
public record TimeLogPeriodsForDevice(Device device, List<TimeLogPeriodsForTeamSet> timeLogPeriodsForTeamSet) {

}
