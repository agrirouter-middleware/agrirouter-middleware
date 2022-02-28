package de.agrirouter.middleware.controller.dto.request;

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
     * The ID of the time log period.
     */
    @Schema(description = "The ID of the time log period.")
    private String timeLogPeriodId;

    /**
     * The beginning of the time interval.
     */
    @Schema(description = "The beginning of the time interval.")
    private Long sendFrom;

    /**
     * The end of the time interval.
     */
    @Schema(description = "The end of the time interval.")
    private Long sendTo;

    /**
     * The DDIs to list, if null or empty all DDIs will be listed and no filter will be applied.
     */
    @Schema(description = "The DDIs to list, if null or empty all DDIs will be listed and no filter will be applied.")
    private Set<String> ddisToList;
}
