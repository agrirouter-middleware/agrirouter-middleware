package de.agrirouter.middleware.controller.unsecured.maintenance;

import de.agrirouter.middleware.business.ApplicationService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.agrirouter.middleware.controller.UnsecuredApiController.API_PREFIX;

/**
 * Controller to manage applications.
 */
@RestController
@Profile("maintenance")
@RequestMapping(API_PREFIX + "/maintenance/application")
@Tag(
        name = "maintenance",
        description = "Maintenance operations for internal usage. Do NOT use this profile in production."
)
public class ApplicationMaintenanceController implements UnsecuredApiController {

    private final ApplicationService applicationService;

    public ApplicationMaintenanceController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Resend capabilities and subscriptions for the endpoint.
     *
     * @return HTTP 201 after completion.
     */
    @DeleteMapping(
            "/{internalApplicationId}"
    )
    @Operation(
            operationId = "maintenance.delete",
            description = "Delete the application incl. all endpoints and other data. Handle with care, the data is lost forever.",
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
    public ResponseEntity<?> delete(@Parameter(description = "The internal application ID.", required = true) @PathVariable String internalApplicationId) {
        applicationService.delete(internalApplicationId);
        return ResponseEntity.ok().build();
    }

}
