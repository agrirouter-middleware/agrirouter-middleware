package de.agrirouter.middleware.controller.dto.request;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import com.dke.data.agrirouter.impl.common.UtcTimeService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

/**
 * The request for searching for files.
 */
@Getter
@Setter
@ToString
@Schema(description = "The request for searching for files.")
public class SearchFilesRequest {

    /**
     * Search for headers only. Enabled by default.
     */
    boolean headersOnly = true;

    /**
     * Filter for dedicated content message types.
     */
    Set<ContentMessageType> contentMessageTypes;

    /**
     * The beginning of the time interval.
     */
    @Schema(description = "The beginning of the time interval. Default value would be 4 weeks ago.")
    private Long sendFrom = UtcTimeService.inThePast(UtcTimeService.FOUR_WEEKS_AGO).toEpochSecond();

    /**
     * The end of the time interval.
     */
    @Schema(description = "The end of the time interval.")
    private Long sendTo = UtcTimeService.now().toEpochSecond();

}
