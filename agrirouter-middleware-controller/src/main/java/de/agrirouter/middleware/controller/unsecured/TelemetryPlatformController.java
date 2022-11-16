package de.agrirouter.middleware.controller.unsecured;

import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.SecuredOnboardProcessService;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller to onboard a single endpoint.
 */
@RestController("unsecured-telemetry-platform-controller")
@RequestMapping(UnsecuredApiController.API_PREFIX + "/telemetry-platform")
@Tag(
        name = "telemetry platform management",
        description = "Operations for telemetry platform management, i.e. onboard process for (virtual) endpoints or revoking endpoints."
)
public class TelemetryPlatformController implements UnsecuredApiController {

    private final ApplicationService applicationService;
    private final SecuredOnboardProcessService securedOnboardProcessService;

    public TelemetryPlatformController(ApplicationService applicationService,
                                       SecuredOnboardProcessService securedOnboardProcessService) {
        this.applicationService = applicationService;
        this.securedOnboardProcessService = securedOnboardProcessService;
    }

    /**
     * Create an authorization URL for the telemetry platform.
     *
     * @param internalApplicationId The internal id of the application.
     * @param externalEndpointId    The id of the endpoint.
     * @param redirectUrl           The redirect URL.
     * @return HTTP 200 with the URL.
     */
    @GetMapping(value = "/{internalApplicationId}/{externalEndpointId}")
    @Operation(
            operationId = "telemetry-platform.auth-url",
            description = "Create an authorization URL for the telemetry platform.",
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "The URL (as redirect) to authorize the creation of an endpoint within the account of the user.",
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
    public RedirectView onboardTelemetryPlatform(@Parameter(description = "The internal ID of the application.", required = true) @PathVariable String internalApplicationId,
                                                 @Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId,
                                                 @Parameter(description = "The redirect URL for this onboard request.") @RequestParam(name = "redirectUrl", required = false) String redirectUrl) {
        final var application = applicationService.find(internalApplicationId);
        final var url = securedOnboardProcessService.generateAuthorizationUrl(application, externalEndpointId, redirectUrl);
        return new RedirectView(url);
    }

}
