package de.agrirouter.middleware.controller.dto.response.domain.timelog.periods;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * A dedicated time log period.
 */
@Getter
@Setter
@Schema(description = "A dedicated time log period.")
public class TimeLogPeriodDto {

    /**
     * Begin of the period.
     */
    @Schema(description = "Begin of the period.")
    private long begin;

    /**
     * End of the period.
     */
    @Schema(description = "End of the period.")
    private long end;

    /**
     * Begin of the period.
     */
    @Schema(description = "Begin of the period.")
    private Instant humanReadableBegin;

    /**
     * End of the period.
     */
    @Schema(description = "End of the period.")
    private Instant humanReadableEnd;

    /**
     * Number Time logs within a period.
     */
    @Schema(description = "Number Time logs within a period.")
    private int nrOfTimeLogs;

}
