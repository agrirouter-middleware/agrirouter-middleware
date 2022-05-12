package de.agrirouter.middleware.controller.dto.request;

import com.dke.data.agrirouter.impl.common.UtcTimeService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * A request to search for telemetry data.
 */
@Getter
@Setter
@ToString
@Schema(description = "A request to search for telemetry data.")
public class SearchTelemetryDataRequest {

    /**
     * The ID of the device.
     */
    @NotNull
    @NotEmpty
    @Schema(description = "The ID of the device.")
    private String internalDeviceId;

    /**
     * The team set context ID.
     */
    @NotNull
    @NotEmpty
    @Schema(description = "The team set context ID.")
    private String teamSetContextId;

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

    /**
     * The DDIs to list, if null or empty all DDIs will be listed and no filter will be applied.
     */
    @Schema(description = "The DDIs to list, if null or empty all DDIs will be listed and no filter will be applied.")
    private Set<Integer> ddisToList;
}
