package de.agrirouter.middleware.controller.unsecured;

import de.agrirouter.middleware.api.errorhandling.ParameterValidationException;
import de.agrirouter.middleware.business.TenantService;
import de.agrirouter.middleware.controller.dto.request.TenantRegistrationRequest;
import de.agrirouter.middleware.controller.dto.response.*;
import de.agrirouter.middleware.controller.dto.response.domain.TenantDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

/**
 * Controller to manage tenants.
 */
@RestController
@Profile("maintenance")
@RequestMapping(UnsecuredApiController.API_PREFIX + "/maintenance/tenant")
@Tag(
        name = "maintenance",
        description = "Maintenance operations for internal usage. Do NOT use this profile in production."
)
public class TenantMaintenanceController implements UnsecuredApiController {

    private final TenantService tenantService;
    private final ModelMapper modelMapper;

    public TenantMaintenanceController(TenantService tenantService,
                                       ModelMapper modelMapper) {
        this.tenantService = tenantService;
        this.modelMapper = modelMapper;
    }

    /**
     * Register a new tenant.
     *
     * @param tenantRegistrationRequest The request to register the tenant.
     * @return HTTP 200 after registration, HTTP 400 with an error code in case of an exception.
     */
    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "maintenance.tenant.register",
            description = "Register a new tenant within the middleware. Can be skipped if you only use the default tenant.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "In case the tenant was created.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = TenantRegistrationResponse.class
                                    )
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
    public ResponseEntity<TenantRegistrationResponse> register(@Parameter(description = "The request holding information to register a new tenant.", required = true) @Valid @RequestBody TenantRegistrationRequest tenantRegistrationRequest,
                                                               @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var tenantRegistrationResult = tenantService.register(tenantRegistrationRequest.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(new TenantRegistrationResponse(tenantRegistrationResult.getTenantId(), tenantRegistrationResult.getAccessToken()));
    }

    /**
     * List all existing tenants.
     *
     * @return all existing tenants.
     */
    @GetMapping(
            value = "/",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "maintenance.tenant.find-all",
            description = "Find all tenants within the middleware.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "In case there are tenants.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = FindTenantResponse.class
                                    )
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
    public ResponseEntity<FindTenantResponse> findAll() {
        final var tenants = tenantService.findAll().stream().map(tenant -> modelMapper.map(tenant, TenantDto.class)).collect(Collectors.toList());
        return ResponseEntity.ok(new FindTenantResponse(tenants));
    }

    /**
     * Reset the password for a tenant.
     *
     * @param tenantId The ID of the tenant.
     * @return HTTP 200 after password reset, HTTP 400 with an error code in case of an exception.
     */
    @PutMapping(
            value = "/reset-password/{tenantId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "maintenance.tenant.reset-password",
            description = "Reset the password for an existing tenant within the middleware.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "In case the password for the tenant was reset.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = TenantRegistrationResponse.class
                                    )
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
    public ResponseEntity<TenantPasswordResetResponse> resetPassword(@Parameter(description = "The tenant ID.", required = true) @PathVariable String tenantId) {
        final var newAccessToken = tenantService.resetPassword(tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TenantPasswordResetResponse(tenantId, newAccessToken));
    }

}
