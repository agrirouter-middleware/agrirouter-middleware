package de.agrirouter.middleware.controller.secured;

import de.agrirouter.middleware.api.errorhandling.ParameterValidationException;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.business.parameters.AddRouterDeviceParameters;
import de.agrirouter.middleware.controller.dto.request.AddRouterDeviceRequest;
import de.agrirouter.middleware.controller.dto.request.AddSupportedTechnicalMessageTypeRequest;
import de.agrirouter.middleware.controller.dto.request.ApplicationRegistrationRequest;
import de.agrirouter.middleware.controller.dto.request.UpdateApplicationRequest;
import de.agrirouter.middleware.controller.dto.response.*;
import de.agrirouter.middleware.controller.dto.response.domain.ApplicationDto;
import de.agrirouter.middleware.controller.dto.response.domain.EndpointWithChildrenDto;
import de.agrirouter.middleware.controller.helper.CreateEndpointStatusHelper;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.ApplicationSettings;
import de.agrirouter.middleware.domain.SupportedTechnicalMessageType;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.*;

/**
 * Controller to manage applications.
 */
@RestController
@Tag(
        name = "application management",
        description = "Operations for the application management, i.e. register, add supported technical message types and status checks."
)
@RequestMapping(SecuredApiController.API_PREFIX + "/application")
public class ApplicationController implements SecuredApiController {

    private final ApplicationService applicationService;
    private final EndpointService endpointService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final ModelMapper modelMapper;

    public ApplicationController(ApplicationService applicationService,
                                 EndpointService endpointService,
                                 MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                 ModelMapper modelMapper) {
        this.applicationService = applicationService;
        this.endpointService = endpointService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.modelMapper = modelMapper;
    }

    /**
     * Register an application.
     *
     * @param applicationRegistrationRequest The application to register.
     * @return HTTP 201 after registration.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "application.register",
            description = "Register an application within the middleware.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "The application has been registered and was created successfully.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = RegisterApplicationResponse.class
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
    public ResponseEntity<RegisterApplicationResponse> register(Principal principal,
                                                                @Parameter(description = "The request parameters to register an application.", required = true) @Valid @RequestBody ApplicationRegistrationRequest applicationRegistrationRequest,
                                                                @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        Application application = new Application();
        application.setApplicationId(applicationRegistrationRequest.getApplicationId());
        application.setVersionId(applicationRegistrationRequest.getVersionId());
        application.setName(applicationRegistrationRequest.getName());
        application.setApplicationType(applicationRegistrationRequest.getApplicationType());

        final var publicKey = new String(Base64.getDecoder().decode(applicationRegistrationRequest.getPublicKey()));
        application.setPublicKey(publicKey);

        final var privateKey = new String(Base64.getDecoder().decode(applicationRegistrationRequest.getPrivateKey()));
        application.setPrivateKey(privateKey);

        final var applicationSettings = new ApplicationSettings();
        applicationSettings.setRedirectUrl(applicationRegistrationRequest.getRedirectUrl());
        application.setApplicationSettings(applicationSettings);

        applicationService.save(principal, application);

        final var applicationDto = new ApplicationDto();
        modelMapper.map(application, applicationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterApplicationResponse(applicationDto));
    }

    /**
     * Update an existing application.
     *
     * @param updateApplicationRequest The application to update.
     * @return HTTP 200 after update.
     */
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "application.update",
            description = "update an existing application within the middleware.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The application has been updated successfully.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = UpdateApplicationResponse.class
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
    public ResponseEntity<RegisterApplicationResponse> update(Principal principal,
                                                              @Parameter(description = "The request parameters to update the existing application.", required = true) @Valid @RequestBody UpdateApplicationRequest updateApplicationRequest,
                                                              @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }

        final var existingApplication = applicationService.find(updateApplicationRequest.getInternalApplicationId());

        if (StringUtils.isNotBlank(updateApplicationRequest.getName())) {
            existingApplication.setName(updateApplicationRequest.getName());
        }

        if (StringUtils.isNotBlank(updateApplicationRequest.getPublicKey())) {
            final var publicKey = new String(Base64.getDecoder().decode(updateApplicationRequest.getPublicKey()));
            existingApplication.setPublicKey(publicKey);
        }

        if (StringUtils.isNotBlank(updateApplicationRequest.getPrivateKey())) {
            final var privateKey = new String(Base64.getDecoder().decode(updateApplicationRequest.getPrivateKey()));
            existingApplication.setPrivateKey(privateKey);
        }

        if (StringUtils.isNotBlank(updateApplicationRequest.getRedirectUrl())) {
            final var applicationSettings = new ApplicationSettings();
            applicationSettings.setRedirectUrl(updateApplicationRequest.getRedirectUrl());
            existingApplication.setApplicationSettings(applicationSettings);
        }

        applicationService.update(principal, existingApplication);

        final var applicationDto = new ApplicationDto();
        modelMapper.map(existingApplication, applicationDto);
        return ResponseEntity.status(HttpStatus.OK).body(new RegisterApplicationResponse(applicationDto));
    }

    /**
     * Define the supported technical message types.
     *
     * @param principal                               The principal to fetch the application.
     * @param applicationId                           The ID of the application.
     * @param addSupportedTechnicalMessageTypeRequest The application to register.
     * @return HTTP 201 after definition.
     */
    @PutMapping(
            value = "/supported-technical-message-types/{applicationId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "application.define-technical-message-types",
            description = "Define the technical types for the application.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "In case the supported technical message types were added."
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
    public ResponseEntity<Void> defineSupportedTechnicalMessageTypes(Principal principal,
                                                                     @Parameter(description = "The ID of the application.", required = true) @PathVariable String applicationId,
                                                                     @Parameter(description = "The container holding the parameters to add the supported technical messages types.", required = true) @Valid @RequestBody AddSupportedTechnicalMessageTypeRequest addSupportedTechnicalMessageTypeRequest,
                                                                     @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        Set<SupportedTechnicalMessageType> supportedTechnicalMessageTypes = new HashSet<>();
        addSupportedTechnicalMessageTypeRequest.getSupportedTechnicalMessageTypes().forEach(dto -> {
            final var supportedTechnicalMessageType = new SupportedTechnicalMessageType();
            supportedTechnicalMessageType.setTechnicalMessageType(dto.getTechnicalMessageType());
            supportedTechnicalMessageType.setDirection(dto.getDirection());
            supportedTechnicalMessageTypes.add(supportedTechnicalMessageType);
        });
        applicationService.defineSupportedTechnicalMessageTypes(principal, applicationId, supportedTechnicalMessageTypes);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Add a router device to the application.
     *
     * @param principal              The principal to fetch the application.
     * @param applicationId          The ID of the application.
     * @param addRouterDeviceRequest The request to register the router device.
     * @return HTTP 201 after definition.
     */
    @PutMapping(
            value = "/router-device/{applicationId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "application.add-router-device",
            description = "Add a router device to the application.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "In case the router device was added."
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
    public ResponseEntity<Void> addRouterDevice(Principal principal,
                                                @Parameter(description = "The ID of the application.", required = true) @PathVariable String applicationId,
                                                @Parameter(description = "The container holding the parameters to add a router device.", required = true) @Valid @RequestBody AddRouterDeviceRequest addRouterDeviceRequest,
                                                @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var addRouterDeviceParameters = new AddRouterDeviceParameters();
        addRouterDeviceParameters.setInternalApplicationId(applicationId);
        addRouterDeviceParameters.setTenantId(principal.getName());
        addRouterDeviceParameters.setDeviceAlternateId(addRouterDeviceRequest.getRouterDevice().getDeviceAlternateId());
        addRouterDeviceParameters.setCertificate(addRouterDeviceRequest.getRouterDevice().getAuthentication().getCertificate());
        addRouterDeviceParameters.setType(addRouterDeviceRequest.getRouterDevice().getAuthentication().getType());
        addRouterDeviceParameters.setSecret(addRouterDeviceRequest.getRouterDevice().getAuthentication().getSecret());
        addRouterDeviceParameters.setHost(addRouterDeviceRequest.getRouterDevice().getConnectionCriteria().getHost());
        addRouterDeviceParameters.setPort(addRouterDeviceRequest.getRouterDevice().getConnectionCriteria().getPort());
        addRouterDeviceParameters.setClientId(addRouterDeviceRequest.getRouterDevice().getConnectionCriteria().getClientId());
        applicationService.addRouterDevice(addRouterDeviceParameters);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Find an application by its ID.
     *
     * @param principal     The principal to fetch the application.
     * @param applicationId The ID of the application.
     * @return HTTP 200 with the data of the application or an HTTP 400 with an error message.
     */
    @GetMapping(
            value = "/{applicationId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "application.find",
            description = "Find a specific application by the given ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "A container holding the application that has been found.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = FindApplicationResponse.class
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
    public ResponseEntity<FindApplicationResponse> find(Principal principal,
                                                        @Parameter(description = "The ID of the application", required = true) @PathVariable String applicationId) {
        final var application = applicationService.find(applicationId, principal);
        final var applicationDto = new ApplicationDto();
        modelMapper.map(application, applicationDto);
        return ResponseEntity.ok(new FindApplicationResponse(applicationDto));
    }

    /**
     * Determine the status for all endpoints within the application.
     *
     * @param principal     The principal to fetch the application.
     * @param applicationId The ID of the application.
     * @return HTTP 200 with the data of the application or an HTTP 400 with an error message.
     */
    @GetMapping(
            value = "/status/{applicationId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "application.status",
            description = "Show the status of an application",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "A container holding the status of the application.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ApplicationStatusResponse.class
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
    public ResponseEntity<ApplicationStatusResponse> status(Principal principal,
                                                            @Parameter(description = "The ID of the application.", required = true) @PathVariable String applicationId) {
        final var application = applicationService.find(applicationId, principal);
        final var applicationStatusResponse = new ApplicationWithEndpointStatusDto();
        modelMapper.map(application, applicationStatusResponse);
        applicationStatusResponse.setEndpointsWithStatus(new ArrayList<>());
        application.getEndpoints()
                .stream()
                .map(endpoint -> CreateEndpointStatusHelper.map(modelMapper, endpointService, applicationService, messageWaitingForAcknowledgementService, endpoint))
                .forEach(endpointWithStatusDto -> applicationStatusResponse.getEndpointsWithStatus().add(endpointWithStatusDto));
        return ResponseEntity.ok(new ApplicationStatusResponse(applicationStatusResponse));
    }

    /**
     * Find all applications.
     *
     * @return All applications.
     */
    @GetMapping(
            value = "/all",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "application.all",
            description = "Search for all applications.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "All applications registered for the current tenant.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = FindApplicationResponse.class
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
    public ResponseEntity<FindApplicationsResponse> findAll(Principal principal) {
        List<Application> applications = applicationService.findAll(principal);
        List<ApplicationDto> findApplicationsResponse = new ArrayList<>();
        applications.forEach(application -> {
            final var applicationDto = new ApplicationDto();
            modelMapper.map(application, applicationDto);
            findApplicationsResponse.add(applicationDto);
        });
        return ResponseEntity.ok(new FindApplicationsResponse(findApplicationsResponse));
    }

    /**
     * Find all endpoints of an application by the IDs of the application.
     *
     * @param applicationId The ID of the application.
     * @return HTTP 200 with the data of the application or an HTTP 400 with an error message.
     */
    @GetMapping(
            value = "/{applicationId}/endpoints",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "application.endpoints",
            description = "Fetch all endpoints for an application.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The endpoints for the application.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = FindEndpointsForApplicationResponse.class
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
    public ResponseEntity<FindEndpointsForApplicationResponse> findEndpointsForApplication(Principal principal,
                                                                                           @Parameter(description = "The ID of the application.", required = true) @PathVariable String applicationId) {
        final var application = applicationService.find(applicationId, principal);
        final var endpointWithChildrenDtos = new ArrayList<EndpointWithChildrenDto>();
        application.getEndpoints().forEach(endpoint -> {
            final var dto = new EndpointWithChildrenDto();
            modelMapper.map(endpoint, dto);
            endpointWithChildrenDtos.add(dto);
        });
        return ResponseEntity.ok(new FindEndpointsForApplicationResponse(endpointWithChildrenDtos));
    }

}
