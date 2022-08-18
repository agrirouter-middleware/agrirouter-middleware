package de.agrirouter.middleware.controller.dto.response.domain.timelog.periods;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * All time log periods.
 */
@Getter
@Setter
@ToString
@Schema(description = "All time log periods.")
public class TimeLogPeriodsDto {

    /**
     * All time log periods.
     */
    @Schema(description = "All time log periods.")
    private List<TimeLogPeriodDto> timeLogPeriods;

}
