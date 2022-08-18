package de.agrirouter.middleware.controller.dto.response.domain.timelog.periods;

import de.agrirouter.middleware.controller.dto.response.domain.DeviceDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * All time log periods for a team set.
 */
@Getter
@Setter
@Schema(description = "Container holding all time log periods for a team set.")
public class TimeLogPeriodDtosForDevice {

    /**
     * The device.
     */
    @Schema(description = "The device the time logs are belonging to.")
    private DeviceDto device;

    /**
     * All time log periods for the belonging team sets.
     */
    @Schema(description = "All time log periods for the belonging team sets.")
    private List<TimeLogPeriodDtosForTeamSet> timeLogPeriodsForTeamSet;

}
