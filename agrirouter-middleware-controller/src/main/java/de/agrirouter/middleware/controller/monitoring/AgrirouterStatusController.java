package de.agrirouter.middleware.controller.monitoring;

import de.agrirouter.middleware.controller.MonitoringApiController;
import de.agrirouter.middleware.controller.dto.response.AgrirouterStatusResponse;
import de.agrirouter.middleware.integration.status.AgrirouterStatusIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for agrirouter© status monitoring.
 */
@Slf4j
@RestController
@Tag(name = "monitoring")
@RequestMapping(MonitoringApiController.API_PREFIX + "/agrirouter")
public class AgrirouterStatusController implements MonitoringApiController {

    private final AgrirouterStatusIntegrationService agrirouterStatusIntegrationService;

    public AgrirouterStatusController(AgrirouterStatusIntegrationService agrirouterStatusIntegrationService) {
        this.agrirouterStatusIntegrationService = agrirouterStatusIntegrationService;
    }

    /**
     * This endpoint is used to check the status of the agrirouter©.
     */
    @Operation(
            operationId = "agrirouter.status",
            description = "Get the current status of the agrirouter©.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The current status of the agrirouter©.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AgrirouterStatusResponse.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "503",
                            description = "In case the agrirouter© is not operational.",
                            content = @Content(
                                    mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AgrirouterStatusResponse.class
                                    )
                            )
                    )
            }
    )
    @GetMapping("/status")
    public ResponseEntity<AgrirouterStatusResponse> status() {
        boolean operational = agrirouterStatusIntegrationService.isOperational();
        if (operational) {
            log.info("Agrirouter© is operational.");
            return ResponseEntity.ok(new AgrirouterStatusResponse());
        } else {
            log.warn("Agrirouter© is not operational.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new AgrirouterStatusResponse());
        }
    }

}
