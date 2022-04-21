package de.agrirouter.middleware.controller.unsecured;

import de.agrirouter.middleware.api.errorhandling.ParameterValidationException;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import de.agrirouter.middleware.controller.dto.response.ParameterValidationProblemResponse;
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
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.agrirouter.middleware.controller.unsecured.UnsecuredApiController.API_PREFIX;

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
    public ResponseEntity<?> resendCapabilitiesAndSubscriptions(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId) {
        endpointService.resendCapabilities(externalEndpointId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
