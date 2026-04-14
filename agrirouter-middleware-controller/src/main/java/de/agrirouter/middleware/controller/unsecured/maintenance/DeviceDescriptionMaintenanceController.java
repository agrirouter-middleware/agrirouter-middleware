package de.agrirouter.middleware.controller.unsecured.maintenance;

import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.business.DeviceDescriptionService;
import de.agrirouter.middleware.controller.UnsecuredApiController;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.agrirouter.middleware.controller.UnsecuredApiController.API_PREFIX;

/**
 * Controller for device description maintenance.
 */
@RestController
@Profile("maintenance")
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + Routes.MaintenanceEndpoints.ALL_REQUESTS + "/device-descriptions")
@Tag(
        name = "maintenance",
        description = "Maintenance operations for internal usage. Do NOT use this profile in production."
)
public class DeviceDescriptionMaintenanceController implements UnsecuredApiController {

    private final DeviceDescriptionService deviceDescriptionService;

    /**
     * Prune all device descriptions for all devices.
     *
     * @return HTTP 200 after pruning.
     */
    @PostMapping(
            value = "/prune",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "maintenance.device-descriptions.prune",
            description = "Prune all device descriptions for all devices within the middleware.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "In case the device descriptions were pruned."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "In case of an unknown error.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            }
    )
    public ResponseEntity<Void> prune() {
        deviceDescriptionService.pruneAll();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
