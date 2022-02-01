package de.agrirouter.middleware.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * A request to search for telemetry data.
 */
@Getter
@Setter
@ToString
@Schema(description = "A request to search for telemetry data.")
public class SearchTelemetryDataPeriodsRequest {

    /**
     * The ID of the endpoint within the middleware.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The ID of the endpoint within the middleware.")
    private String externalEndpointId;

    /**
     * The list of internal device IDs to search for.
     */
    @Schema(description = "The list of internal device IDs to search for.")
    private List<String> internalDeviceIds;

    /**
     * Filter empty entries, like machines and time periods.
     */
    @Schema(description = "Filter empty entries, like machines and time periods.")
    private boolean filterEmptyEntries;

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

}
