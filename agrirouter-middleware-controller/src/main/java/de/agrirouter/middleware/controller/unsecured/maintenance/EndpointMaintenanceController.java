package de.agrirouter.middleware.controller.unsecured.maintenance;

import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import de.agrirouter.middleware.controller.dto.response.ParameterValidationProblemResponse;
import de.agrirouter.middleware.controller.UnsecuredApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static de.agrirouter.middleware.controller.UnsecuredApiController.API_PREFIX;

/**
 * Controller to manage endpoints.
 */
@RestController
@Profile("maintenance")
@RequestMapping(API_PREFIX + "/maintenance/endpoint")
@Tag(
        name = "maintenance",
        description = "Maintenance operations for internal usage. Do NOT use this profile in production."
)
public class EndpointMaintenanceController implements UnsecuredApiController {

    private final EndpointService endpointService;

    public EndpointMaintenanceController(EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    /**
     * Resend capabilities and subscriptions for the endpoint.
     *
     * @return HTTP 201 after completion.
     */
    @PutMapping(
            "/resend/capabilities-and-subscriptions/{externalEndpointId}"
    )
    @Operation(
            operationId = "maintenance.resend-capabilities",
            description = "Resend the capabilities and subscriptions for the endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "In case the operation was successful.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a business exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a parameter validation exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ParameterValidationProblemResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
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
    public ResponseEntity<Void> resendCapabilitiesAndSubscriptions(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId) {
        endpointService.resendCapabilities(externalEndpointId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Reset errors for the endpoint.
     *
     * @return HTTP 201 after completion.
     */
    @DeleteMapping(
            "/reset/errors/{externalEndpointId}"
    )
    @Operation(
            operationId = "maintenance.reset-errors",
            description = "Reset the errors for the endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "In case the operation was successful.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a business exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a parameter validation exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ParameterValidationProblemResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
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
    public ResponseEntity<Void> resetErrors(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId) {
        endpointService.resetErrors(externalEndpointId);
        return ResponseEntity.ok().build();
    }

    /**
     * Reset warnings for the endpoint.
     *
     * @return HTTP 201 after completion.
     */
    @DeleteMapping(
            "/reset/warnings/{externalEndpointId}"
    )
    @Operation(
            operationId = "maintenance.reset-warnings",
            description = "Reset the warnings for the endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "In case the operation was successful.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a business exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a parameter validation exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ParameterValidationProblemResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
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
    public ResponseEntity<Void> resetWarnings(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId) {
        endpointService.resetWarnings(externalEndpointId);
        return ResponseEntity.ok().build();
    }


    /**
     * Reset warnings for the endpoint.
     *
     * @return HTTP 201 after completion.
     */
    @DeleteMapping(
            "/{externalEndpointId}"
    )
    @Operation(
            operationId = "maintenance.delete-endpoint",
            description = "Delete the endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "In case the operation was successful.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a business exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a parameter validation exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ParameterValidationProblemResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
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
    public ResponseEntity<Void> deleteEndpoint(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId) {
        endpointService.deleteAllEndpoints(externalEndpointId);
        return ResponseEntity.ok().build();
    }

}
