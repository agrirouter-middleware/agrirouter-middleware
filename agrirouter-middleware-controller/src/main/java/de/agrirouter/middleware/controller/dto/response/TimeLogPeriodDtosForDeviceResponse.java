package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.timelog.periods.TimeLogPeriodDtosForDevice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * Response class for better API design.
 */
@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "The response holding all the time log periods for the device.")
public class TimeLogPeriodDtosForDeviceResponse extends Response {

    /**
     * The number of devices.
     */
    @Schema(description = "The number of devices.")
    long nrOfDevices;

    /**
     * The time log periods.
     */
    @Schema(description = "The time log periods for the devices. Grouped by device.")
    List<TimeLogPeriodDtosForDevice> timeLogPeriodsForDevices;
}
