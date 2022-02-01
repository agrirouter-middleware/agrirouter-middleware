package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.timelog.TimeLogWithRawDataDto;
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
@Schema(description = "Response holding the time logs after searching for them.")
public class TimeLogSearchResponse extends Response {

    /**
     * The number of time logs.
     */
    @Schema(description = "The number of time logs.")
    int numberOfTimeLogs;

    /**
     * The time log with raw data.
     */
    @Schema(description = "The time log with raw data.")
    List<TimeLogWithRawDataDto> timeLogsWithRawData;

}
