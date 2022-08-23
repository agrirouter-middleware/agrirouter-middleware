package de.agrirouter.middleware.controller.dto.response.domain.timelog.periods;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * All time log periods for a team set.
 */
@Getter
@Setter
@Schema(description = "All time log periods for a team set.")
public class TimeLogPeriodDtosForTeamSet {

    /**
     * The team set context ID.
     */
    @Schema(description = "The team set context ID.")
    private String teamSetContextId;

    /**
     * All time log periods.
     */
    @Schema(description = "All time log periods.")
    private TimeLogPeriodsDto timeLogPeriods;
}
