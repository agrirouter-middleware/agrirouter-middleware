package de.agrirouter.middleware.business.dto.timelog.periods;

import de.agrirouter.middleware.domain.documents.Device;
import lombok.Getter;

import java.util.List;

/**
 * All time log periods for a team set.
 */
@Getter
public class TimeLogPeriodsForDevice {

    /**
     * The device.
     */
    private final Device device;

    /**
     * All time log periods for the belonging team sets.
     */
    private final List<TimeLogPeriodsForTeamSet> timeLogPeriodsForTeamSet;

    public TimeLogPeriodsForDevice(Device device, List<TimeLogPeriodsForTeamSet> timeLogPeriodsForTeamSet) {
        this.device = device;
        this.timeLogPeriodsForTeamSet = timeLogPeriodsForTeamSet;
    }
}
