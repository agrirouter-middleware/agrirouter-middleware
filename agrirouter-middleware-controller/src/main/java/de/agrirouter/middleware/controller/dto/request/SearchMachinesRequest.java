package de.agrirouter.middleware.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * A request to search for machines.
 */
@Getter
@Setter
@ToString
@Schema(description = "A request to search for machines.")
public class SearchMachinesRequest {

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
     * Show the current device description.
     */
    @Schema(description = "Show the current device description.")
    private boolean withCurrentDeviceDescription;

}
