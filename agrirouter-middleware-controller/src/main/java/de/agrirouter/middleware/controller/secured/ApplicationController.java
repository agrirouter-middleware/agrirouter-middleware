package de.agrirouter.middleware.controller.secured;

import de.agrirouter.middleware.api.errorhandling.ParameterValidationException;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.business.cache.messaging.MessageCache;
import de.agrirouter.middleware.business.parameters.AddRouterDeviceParameters;
import de.agrirouter.middleware.controller.SecuredApiController;
import de.agrirouter.middleware.controller.dto.request.ApplicationRegistrationRequest;
import de.agrirouter.middleware.controller.dto.request.UpdateApplicationRequest;
import de.agrirouter.middleware.controller.dto.response.*;
import de.agrirouter.middleware.controller.dto.response.domain.ApplicationDto;
import de.agrirouter.middleware.controller.dto.response.domain.ApplicationWithEndpointStatusDto;
import de.agrirouter.middleware.controller.dto.response.domain.EndpointWithChildrenDto;
import de.agrirouter.middleware.controller.helper.EndpointStatusHelper;
import de.agrirouter.middleware.domain.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller to manage applications.
 */
@RestController
@RequiredArgsConstructor
@Tag(
        name = "application management",
        description = "Operations for the application management, i.e. register, add supported technical message types and status checks."
)
@RequestMapping(SecuredApiController.API_PREFIX + "/application")
public class ApplicationController implements SecuredApiController {

    private final ApplicationService applicationService;
    private final EndpointService endpointService;
    private final ModelMapper modelMapper;

    private final MessageCache messageCache;

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
        var application = createApplicationFromRequest(applicationRegistrationRequest);
        applicationService.save(principal, application);
        final var applicationDto = new ApplicationDto();
        modelMapper.map(application, applicationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterApplicationResponse(applicationDto));
    }

    @NotNull
    private static Application createApplicationFromRequest(ApplicationRegistrationRequest applicationRegistrationRequest) {
        Application application = new Application();
        application.setApplicationId(applicationRegistrationRequest.getApplicationId());
        application.setVersionId(applicationRegistrationRequest.getVersionId());
        application.setName(applicationRegistrationRequest.getName());
        application.setApplicationType(applicationRegistrationRequest.getApplicationType());

        final var publicKey = new String(Base64.getDecoder().decode(applicationRegistrationRequest.getBase64EncodedPublicKey()));
        application.setPublicKey(publicKey);

        final var privateKey = new String(Base64.getDecoder().decode(applicationRegistrationRequest.getBase64EncodedPrivateKey()));
        application.setPrivateKey(privateKey);

        final var supportedTechnicalMessageTypes = createTechnicalMessageTypesFromRequest(applicationRegistrationRequest);
        application.setSupportedTechnicalMessageTypes(supportedTechnicalMessageTypes);

        final var applicationSettings = new ApplicationSettings();
        applicationSettings.setRedirectUrl(applicationRegistrationRequest.getRedirectUrl());
        applicationSettings.setRouterDevice(createRouterDeviceFromRequest(applicationRegistrationRequest));
        application.setApplicationSettings(applicationSettings);

        return application;
    }

    @NotNull
    private static RouterDevice createRouterDeviceFromRequest(ApplicationRegistrationRequest applicationRegistrationRequest) {
        final var routerDevice = new RouterDevice();
        routerDevice.setDeviceAlternateId(applicationRegistrationRequest.getRouterDevice().getDeviceAlternateId());
        final var authentication = new Authentication();
        authentication.setCertificate(applicationRegistrationRequest.getRouterDevice().getAuthentication().getCertificate());
        authentication.setSecret(applicationRegistrationRequest.getRouterDevice().getAuthentication().getSecret());
        authentication.setType(applicationRegistrationRequest.getRouterDevice().getAuthentication().getType());
        routerDevice.setAuthentication(authentication);
        final var connectionCriteria = new ConnectionCriteria();
        connectionCriteria.setClientId(applicationRegistrationRequest.getRouterDevice().getConnectionCriteria().getClientId());
        connectionCriteria.setHost(applicationRegistrationRequest.getRouterDevice().getConnectionCriteria().getHost());
        connectionCriteria.setPort(applicationRegistrationRequest.getRouterDevice().getConnectionCriteria().getPort());
        routerDevice.setConnectionCriteria(connectionCriteria);
        return routerDevice;
    }

    @NotNull
    private static Set<SupportedTechnicalMessageType> createTechnicalMessageTypesFromRequest(ApplicationRegistrationRequest applicationRegistrationRequest) {
        Set<SupportedTechnicalMessageType> supportedTechnicalMessageTypes = new HashSet<>();
        applicationRegistrationRequest.getSupportedTechnicalMessageTypes().forEach(dto -> {
            final var supportedTechnicalMessageType = new SupportedTechnicalMessageType();
            supportedTechnicalMessageType.setTechnicalMessageType(dto.getTechnicalMessageType());
            supportedTechnicalMessageType.setDirection(dto.getDirection());
            supportedTechnicalMessageTypes.add(supportedTechnicalMessageType);
        });
        return supportedTechnicalMessageTypes;
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
    public ResponseEntity<RegisterApplicationResponse> update(@Parameter(description = "The request parameters to update the existing application.", required = true) @Valid @RequestBody UpdateApplicationRequest updateApplicationRequest,
                                                              @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }

        final var existingApplication = applicationService.find(updateApplicationRequest.getInternalApplicationId());

        if (StringUtils.isNotBlank(updateApplicationRequest.getName())) {
            existingApplication.setName(updateApplicationRequest.getName());
        }

        if (StringUtils.isNotBlank(updateApplicationRequest.getBase64EncodedPublicKey())) {
            final var publicKey = new String(Base64.getDecoder().decode(updateApplicationRequest.getBase64EncodedPublicKey()));
            existingApplication.setPublicKey(publicKey);
        }

        if (StringUtils.isNotBlank(updateApplicationRequest.getBase64EncodedPrivateKey())) {
            final var privateKey = new String(Base64.getDecoder().decode(updateApplicationRequest.getBase64EncodedPrivateKey()));
            existingApplication.setPrivateKey(privateKey);
        }

        if (StringUtils.isNotBlank(updateApplicationRequest.getRedirectUrl())) {
            final var applicationSettings = new ApplicationSettings();
            applicationSettings.setRedirectUrl(updateApplicationRequest.getRedirectUrl());
            existingApplication.setApplicationSettings(applicationSettings);
        }

        applicationService.update(existingApplication);

        if (updateApplicationRequest.getRouterDevice() != null) {
            final var addRouterDeviceParameters = new AddRouterDeviceParameters();
            addRouterDeviceParameters.setInternalApplicationId(updateApplicationRequest.getInternalApplicationId());
            addRouterDeviceParameters.setTenantId(existingApplication.getTenant().getTenantId());
            addRouterDeviceParameters.setDeviceAlternateId(updateApplicationRequest.getRouterDevice().getDeviceAlternateId());
            addRouterDeviceParameters.setCertificate(updateApplicationRequest.getRouterDevice().getAuthentication().getCertificate());
            addRouterDeviceParameters.setType(updateApplicationRequest.getRouterDevice().getAuthentication().getType());
            addRouterDeviceParameters.setSecret(updateApplicationRequest.getRouterDevice().getAuthentication().getSecret());
            addRouterDeviceParameters.setHost(updateApplicationRequest.getRouterDevice().getConnectionCriteria().getHost());
            addRouterDeviceParameters.setPort(updateApplicationRequest.getRouterDevice().getConnectionCriteria().getPort());
            addRouterDeviceParameters.setClientId(updateApplicationRequest.getRouterDevice().getConnectionCriteria().getClientId());
            applicationService.addRouterDevice(addRouterDeviceParameters);
        }

        final var applicationDto = new ApplicationDto();
        modelMapper.map(existingApplication, applicationDto);
        return ResponseEntity.status(HttpStatus.OK).body(new RegisterApplicationResponse(applicationDto));
    }

    /**
     * Find an application by its ID.
     *
     * @param principal             The principal to fetch the application.
     * @param internalApplicationId The internal ID of the application.
     * @return HTTP 200 with the data of the application or an HTTP 400 with an error message.
     */
    @GetMapping(
            value = "/{internalApplicationId}",
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
                                                        @Parameter(description = "The internal ID of the application", required = true) @PathVariable String internalApplicationId) {
        final var application = applicationService.find(internalApplicationId, principal);
        final var applicationDto = new ApplicationDto();
        modelMapper.map(application, applicationDto);
        return ResponseEntity.ok(new FindApplicationResponse(applicationDto));
    }

    /**
     * Determine the status for all endpoints within the application.
     *
     * @param principal             The principal to fetch the application.
     * @param internalApplicationId The internal ID of the application.
     * @return HTTP 200 with the data of the application or an HTTP 400 with an error message.
     */
    @GetMapping(
            value = "/status/{internalApplicationId}",
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
                                                            @Parameter(description = "The internal ID of the application.", required = true) @PathVariable String internalApplicationId) {
        final var application = applicationService.find(internalApplicationId, principal);
        final var applicationStatusResponse = new ApplicationWithEndpointStatusDto();
        modelMapper.map(application, applicationStatusResponse);
        applicationStatusResponse.setUsesRouterDevice(application.usesRouterDevice());
        var endpoints = application.getEndpoints()
                .stream()
                .map(endpoint -> EndpointStatusHelper.mapEndpointWithApplicationDetails(modelMapper, applicationService, endpointService, messageCache, endpoint))
                .collect(Collectors.toList());
        applicationStatusResponse.setEndpoints(endpoints);
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
     * @param internalApplicationId The internal ID of the application.
     * @return HTTP 200 with the data of the application or an HTTP 400 with an error message.
     */
    @GetMapping(
            value = "/{internalApplicationId}/endpoints",
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
                                                                                           @Parameter(description = "The internal ID of the application.", required = true) @PathVariable String internalApplicationId) {
        final var application = applicationService.find(internalApplicationId, principal);
        final var endpointWithChildrenDtos = new ArrayList<EndpointWithChildrenDto>();
        application.getEndpoints().forEach(endpoint -> {
            final var dto = new EndpointWithChildrenDto();
            modelMapper.map(endpoint, dto);
            endpointWithChildrenDtos.add(dto);
        });
        return ResponseEntity.ok(new FindEndpointsForApplicationResponse(endpointWithChildrenDtos));
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
