package de.agrirouter.middleware.controller.secured;

import de.agrirouter.middleware.api.errorhandling.ParameterValidationException;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.business.VirtualOffboardProcessService;
import de.agrirouter.middleware.business.VirtualOnboardProcessService;
import de.agrirouter.middleware.business.parameters.VirtualOffboardProcessParameters;
import de.agrirouter.middleware.business.parameters.VirtualOnboardProcessParameters;
import de.agrirouter.middleware.controller.SecuredApiController;
import de.agrirouter.middleware.controller.dto.request.OnboardVirtualEndpointRequest;
import de.agrirouter.middleware.controller.dto.request.RevokeVirtualEndpointRequest;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import de.agrirouter.middleware.controller.dto.response.ParameterValidationProblemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.core.MediaType;

/**
 * Controller to onboard a single endpoint.
 */
@RestController
@RequestMapping(SecuredApiController.API_PREFIX + "/telemetry-platform/")
@Tag(
        name = "telemetry platform management",
        description = "Operations for telemetry platform management, i.e. onboard process for (virtual) endpoints or revoking endpoints."
)
public class TelemetryPlatformController implements SecuredApiController {

    private final VirtualOnboardProcessService virtualOnboardProcessService;
    private final VirtualOffboardProcessService virtualOffboardProcessService;
    private final EndpointService endpointService;

    public TelemetryPlatformController(VirtualOnboardProcessService virtualOnboardProcessService,
                                       VirtualOffboardProcessService virtualOffboardProcessService,
                                       EndpointService endpointService) {
        this.virtualOnboardProcessService = virtualOnboardProcessService;
        this.virtualOffboardProcessService = virtualOffboardProcessService;
        this.endpointService = endpointService;
    }

    /**
     * Onboard a virtual endpoint using the internal ID of the endpoint.
     *
     * @param externalEndpointId            The external ID of the endpoint.
     * @param onboardVirtualEndpointRequest The body containing all necessary information.
     * @return HTTP 200 in case everything is fine.
     */
    @PostMapping(
            value = "/{externalEndpointId}/virtual",
            consumes = MediaType.APPLICATION_JSON
    )
    @Operation(
            operationId = "telemetry-platform.onboard.virtual",
            description = "Onboard a virtual endpoint for the given telemetry platform.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "The response if the endpoint was created successfully.",
                            content = @Content(
                                    mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a business exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a parameter validation exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ParameterValidationProblemResponse.class
                                    ),
                                    mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "In case of an unknown error.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            }
    )
    public ResponseEntity<Void> onboard(@Parameter(description = "The external ID of the existing endpoint.", required = true) @PathVariable String externalEndpointId,
                                        @Parameter(description = "Necessary information to create the virtual endpoint.", required = true) @Valid @RequestBody OnboardVirtualEndpointRequest onboardVirtualEndpointRequest, @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var virtualOnboardProcessParameters = new VirtualOnboardProcessParameters();
        virtualOnboardProcessParameters.setExternalEndpointId(externalEndpointId);
        virtualOnboardProcessParameters.setExternalVirtualEndpointId(onboardVirtualEndpointRequest.getExternalVirtualEndpointId());
        virtualOnboardProcessParameters.setEndpointName(onboardVirtualEndpointRequest.getEndpointName());
        virtualOnboardProcessService.onboard(virtualOnboardProcessParameters);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Offboard one or multiple virtual CUs.
     *
     * @param externalEndpointId           The external ID of the endpoint.
     * @param revokeVirtualEndpointRequest The body containing all necessary information.
     * @return HTTP 200 in case everything is fine.
     */
    @DeleteMapping(
            value = "/{externalEndpointId}/virtual",
            consumes = MediaType.APPLICATION_JSON
    )
    @Operation(
            operationId = "telemetry-platform.revoke.virtual",
            description = "Revoke a virtual endpoint for the given telemetry platform.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The response if the endpoint was revoked successfully.",
                            content = @Content(
                                    mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a business exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a parameter validation exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ParameterValidationProblemResponse.class
                                    ),
                                    mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "In case of an unknown error.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            }
    )
    public ResponseEntity<Void> revoke(@Parameter(description = "The external ID of the existing endpoint.", required = true) @PathVariable String externalEndpointId,
                                       @Parameter(description = "The necessary information to revoke the virtual endpoint.") @Valid @RequestBody RevokeVirtualEndpointRequest revokeVirtualEndpointRequest,
                                       @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var virtualOffboardProcessParameters = new VirtualOffboardProcessParameters();
        virtualOffboardProcessParameters.setExternalEndpointId(externalEndpointId);
        virtualOffboardProcessParameters.setExternalVirtualEndpointIds(revokeVirtualEndpointRequest.getExternalEndpointIds());
        virtualOffboardProcessService.offboard(virtualOffboardProcessParameters);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Remove the endpoint from the AR and delete the data.
     *
     * @return HTTP 200.
     */
    @DeleteMapping("/{externalEndpointId}")
    @Operation(
            operationId = "telemetry-platform.revoke-process",
            description = "Revoke a telemetry platform from the middleware and remove all of its data, incl. virtual endpoints, messages, logs, etc.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The revoke process was successful.",
                            content = @Content(
                                    mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a business exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "In case of an unknown error.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            }
    )
    public ResponseEntity<Void> revokeTelemetryPlatform(@Parameter(description = "The external endpoint id.", required = true) @PathVariable String externalEndpointId) {
        endpointService.revoke(externalEndpointId);
        return ResponseEntity.ok().build();
    }

}
