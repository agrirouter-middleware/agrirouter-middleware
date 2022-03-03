package de.agrirouter.middleware.controller.unsecured;


import com.dke.data.agrirouter.api.service.onboard.secured.AuthorizationRequestService;
import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.ParameterValidationException;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.SecuredOnboardProcessService;
import de.agrirouter.middleware.business.global.OnboardStateContainer;
import de.agrirouter.middleware.business.parameters.OnboardProcessParameters;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import de.agrirouter.middleware.controller.dto.response.ParameterValidationProblemResponse;
import de.agrirouter.middleware.controller.dto.response.enums.OnboardProcessResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Callback for the secured onboard process.
 */
@Slf4j
@RestController
@RequestMapping(UnsecuredApiController.API_PREFIX + "/callback")
@Tag(
        name = "agrirouter callback",
        description = "The callback for the agrirouter. This callback has to be referenced within the newly created application."
)
public class CallbackController implements UnsecuredApiController {

    private final OnboardStateContainer onboardStateContainer;
    private final ApplicationService applicationService;
    private final SecuredOnboardProcessService securedOnboardProcessService;
    private final AuthorizationRequestService authorizationRequestService;

    public CallbackController(OnboardStateContainer onboardStateContainer,
                              ApplicationService applicationService,
                              SecuredOnboardProcessService securedOnboardProcessService,
                              AuthorizationRequestService authorizationRequestService) {
        this.onboardStateContainer = onboardStateContainer;
        this.applicationService = applicationService;
        this.securedOnboardProcessService = securedOnboardProcessService;
        this.authorizationRequestService = authorizationRequestService;
    }

    /**
     * Callback for the onboard process.
     *
     * @param state     The current state.
     * @param token     The token from the AR.
     * @param signature The signature of the AR.
     */
    @GetMapping
    @Operation(
            operationId = "callback.callback",
            description = "The callback for the onboard process. Used by the agrirouter to send the onboard process data.",
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "In case of a successful response, the callback redirects to an internal page to show the result of the onboard process.",
                            content = @Content(
                                    schema = @Schema(
                                            hidden = true
                                    ),
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
    public RedirectView callback(@Parameter(description = "The state to identify the request internally.", required = true) @RequestParam(value = "state") String state,
                                 @Parameter(description = "Token containing the information for the onboard process, generated by the agrirouter.") @RequestParam(value = "token", required = false) String token,
                                 @Parameter(description = "Signature from the agrirouter, used for validation.") @RequestParam(value = "signature", required = false) String signature,
                                 @Parameter(description = "Error information, in case the onboard process failed.") @RequestParam(value = "error", required = false) String error) {
        final var optionalOnboardState = onboardStateContainer.pop(state);
        if (optionalOnboardState.isPresent()) {
            final var onboardState = optionalOnboardState.get();
            final var application = applicationService.find(onboardState.getInternalApplicationId());
            if (StringUtils.isBlank(error)) {
                // FIXME Check the signature of the AR.
                log.debug("Checking for state >>> {}", state);
                log.debug("Proceeding callback for the onboard process of the following application [{}]", onboardState.getInternalApplicationId());
                final var authorizationResponseToken = authorizationRequestService.decodeToken(token);
                log.trace("Decoded the token >>> {}", authorizationResponseToken);
                final var onboardProcessParameters = new OnboardProcessParameters();
                onboardProcessParameters.setInternalApplicationId(application.getInternalApplicationId());
                onboardProcessParameters.setRegistrationCode(authorizationResponseToken.getRegcode());
                onboardProcessParameters.setExternalEndpointId(onboardState.getExternalEndpointId());
                onboardProcessParameters.setTenantId(onboardState.getTenantId());
                onboardProcessParameters.setAccountId(authorizationResponseToken.getAccount());
                try {
                    securedOnboardProcessService.onboard(onboardProcessParameters);
                    if (StringUtils.isNotBlank(onboardState.getRedirectUrlAfterCallback())) {
                        return new RedirectView(onboardState.getRedirectUrlAfterCallback().concat("?onboardProcessResult=" + OnboardProcessResult.SUCCESS));
                    } else {
                        return new RedirectView(Routes.ONBOARD_PROCESS_RESULT.concat("?onboardProcessResult=" + OnboardProcessResult.SUCCESS), true);
                    }
                } catch (BusinessException e) {
                    log.error("There was an error during the onboard process. Could not handle the request.", e);
                    if (StringUtils.isNotBlank(onboardState.getRedirectUrlAfterCallback())) {
                        return new RedirectView(application.getApplicationSettings().getRedirectUrl().concat("?onboardProcessResult=" + OnboardProcessResult.FAILURE));
                    } else {
                        return new RedirectView(Routes.ONBOARD_PROCESS_RESULT.concat("?onboardProcessResult=" + OnboardProcessResult.FAILURE), true);
                    }
                }
            } else {
                log.error("There was an error during the onboard process. Could not handle the request. The error was '{}'", error);
                if (StringUtils.isNotBlank(onboardState.getRedirectUrlAfterCallback())) {
                    return new RedirectView(application.getApplicationSettings().getRedirectUrl().concat("?onboardProcessResult=" + OnboardProcessResult.FAILURE));
                } else {
                    return new RedirectView(Routes.ONBOARD_PROCESS_RESULT.concat("?onboardProcessResult=" + OnboardProcessResult.FAILURE), true);
                }
            }
        } else {
            log.error("The state for the onboard process was not found, skipping the callback.");
        }
        return null;
    }

}
