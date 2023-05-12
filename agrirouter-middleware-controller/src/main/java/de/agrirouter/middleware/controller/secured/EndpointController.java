package de.agrirouter.middleware.controller.secured;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.ParameterValidationException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorKey;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.business.cache.cloud.CloudOnboardingFailureCache;
import de.agrirouter.middleware.business.cache.messaging.MessageCache;
import de.agrirouter.middleware.controller.SecuredApiController;
import de.agrirouter.middleware.controller.dto.request.EndpointHealthStatusRequest;
import de.agrirouter.middleware.controller.dto.request.EndpointStatusRequest;
import de.agrirouter.middleware.controller.dto.response.*;
import de.agrirouter.middleware.controller.dto.response.domain.*;
import de.agrirouter.middleware.controller.helper.EndpointStatusHelper;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.status.AgrirouterStatusIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller to manage applications.
 */
@RestController
@RequestMapping(SecuredApiController.API_PREFIX + "/endpoint")
@Tag(
        name = "endpoint management",
        description = "Operations for the endpoint management, i.e. status checking or searching for endpoints."
)
public class EndpointController implements SecuredApiController {

    private final ApplicationService applicationService;
    private final EndpointService endpointService;
    private final ModelMapper modelMapper;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final MessageCache messageCache;
    private final MqttClientManagementService mqttClientManagementService;
    private final CloudOnboardingFailureCache cloudOnboardingFailureCache;
    private final AgrirouterStatusIntegrationService agrirouterStatusIntegrationService;

    public EndpointController(ApplicationService applicationService,
                              EndpointService endpointService,
                              ModelMapper modelMapper,
                              MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                              MessageCache messageCache,
                              MqttClientManagementService mqttClientManagementService,
                              CloudOnboardingFailureCache cloudOnboardingFailureCache,
                              AgrirouterStatusIntegrationService agrirouterStatusIntegrationService) {
        this.applicationService = applicationService;
        this.endpointService = endpointService;
        this.modelMapper = modelMapper;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.messageCache = messageCache;
        this.mqttClientManagementService = mqttClientManagementService;
        this.cloudOnboardingFailureCache = cloudOnboardingFailureCache;
        this.agrirouterStatusIntegrationService = agrirouterStatusIntegrationService;
    }

    /**
     * Find an endpoint status by the given IDs of the endpoint.
     *
     * @param endpointStatusRequest The request containing the IDs of the endpoints.
     * @return HTTP 200 with the data of the endpoint or an HTTP 400 with an error message.
     */
    @PostMapping(
            value = "/status",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "endpoint.status",
            description = "Fetch the status of an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The status information for this endpoint.",
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
    public ResponseEntity<EndpointStatusResponse> status(@Parameter(description = "The to search for one or multiple endpoints.", required = true) @Valid @RequestBody EndpointStatusRequest endpointStatusRequest,
                                                         @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var endpoints = endpointService.findByExternalEndpointIds(endpointStatusRequest.getExternalEndpointIds());
        final var mappedEndpoints = new HashMap<String, EndpointWithStatusDto>();
        endpoints.forEach(endpoint -> {
            final var endpointWithStatusDto = EndpointStatusHelper.mapEndpointStatus(modelMapper, applicationService, messageCache, endpoint);
            mappedEndpoints.put(endpoint.getExternalEndpointId(), endpointWithStatusDto);
        });
        return ResponseEntity.ok(new EndpointStatusResponse(mappedEndpoints));
    }

    /**
     * Check the cloud onboarding failures for the given IDs of the virtual endpoint.
     *
     * @param externalVirtualEndpointId The external virtual endpoint ID.
     * @return HTTP 200 with the data of the endpoint or error message otherwise.
     */
    @GetMapping(
            value = "/failures/cloud-onboarding/{externalVirtualEndpointId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "endpoint.failures.cloud-onboarding",
            description = "Fetch the cloud-onboarding failures for the virtual endpoint id.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The status information for this endpoint.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "204",
                            description = "In case there is no failure.",
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
    public ResponseEntity<CloudOnboardingFailureResponse> cloudOnboardingFailures(@Parameter(description = "The external virtual endpoint ID.", required = true) @PathVariable("externalVirtualEndpointId") String externalVirtualEndpointId) {
        Optional<CloudOnboardingFailureCache.FailureEntry> optionalFailureEntry = cloudOnboardingFailureCache.get(externalVirtualEndpointId);
        if (optionalFailureEntry.isPresent()) {
            CloudOnboardingFailureCache.FailureEntry failureEntry = optionalFailureEntry.get();
            final var cloudOnboardFailure = new CloudOnboardingFailureDto();
            cloudOnboardFailure.setErrorCode(failureEntry.errorCode());
            cloudOnboardFailure.setErrorMessage(failureEntry.errorMessage());
            cloudOnboardFailure.setExternalEndpointId(failureEntry.externalEndpointId());
            cloudOnboardFailure.setTimestamp(failureEntry.timestamp());
            cloudOnboardFailure.setVirtualExternalEndpointId(failureEntry.virtualExternalEndpointId());
            return ResponseEntity.ok(new CloudOnboardingFailureResponse(cloudOnboardFailure));
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * Check the connection status for the given IDs of the endpoint.
     *
     * @param endpointStatusRequest The request containing the IDs of the endpoints.
     * @return HTTP 200 with the data of the endpoint or an HTTP 400 with an error message.
     */
    @PostMapping(
            value = "/status/connection",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "endpoint.status.connection",
            description = "Fetch the connection status of an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The status information for this endpoint.",
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
    public ResponseEntity<EndpointConnectionStatusResponse> connectionStatus(@Parameter(description = "The to search for one or multiple endpoints.", required = true) @Valid @RequestBody EndpointStatusRequest endpointStatusRequest,
                                                                             @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var endpoints = endpointService.findByExternalEndpointIds(endpointStatusRequest.getExternalEndpointIds());
        final var mappedEndpoints = new HashMap<String, EndpointConnectionStatusDto>();
        endpoints.forEach(endpoint -> {
            final var endpointWithStatusDto = EndpointStatusHelper.mapConnectionStatus(modelMapper, endpointService, endpoint);
            mappedEndpoints.put(endpoint.getExternalEndpointId(), endpointWithStatusDto);
        });
        return ResponseEntity.ok(new EndpointConnectionStatusResponse(mappedEndpoints));
    }

    /**
     * Check the warnings for the given IDs of the endpoint.
     *
     * @param endpointStatusRequest The request containing the IDs of the endpoints.
     * @return HTTP 200 with the data of the endpoint or an HTTP 400 with an error message.
     */
    @PostMapping(
            value = "/status/warnings",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "endpoint.status.warnings",
            description = "Fetch the warnings of an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The status information for this endpoint.",
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
    public ResponseEntity<EndpointWarningsResponse> warnings(@Parameter(description = "The to search for one or multiple endpoints.", required = true) @Valid @RequestBody EndpointStatusRequest endpointStatusRequest,
                                                             @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var endpoints = endpointService.findByExternalEndpointIds(endpointStatusRequest.getExternalEndpointIds());
        final var mappedEndpoints = new HashMap<String, EndpointWarningsDto>();
        endpoints.forEach(endpoint -> {
            final var endpointWithStatusDto = EndpointStatusHelper.mapWarnings(modelMapper, endpointService, endpoint);
            mappedEndpoints.put(endpoint.getExternalEndpointId(), endpointWithStatusDto);
        });
        return ResponseEntity.ok(new EndpointWarningsResponse(mappedEndpoints));
    }

    /**
     * Fetch the errors for the endpoint.
     *
     * @param endpointStatusRequest -
     * @param errors                -
     * @return -
     */
    @PostMapping(
            value = "/status/errors",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "endpoint.status.errors",
            description = "Fetch the errors of an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The status information for this endpoint.",
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
    public ResponseEntity<EndpointErrorsResponse> errors(@Parameter(description = "The to search for one or multiple endpoints.", required = true) @Valid @RequestBody EndpointStatusRequest endpointStatusRequest,
                                                         @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var endpoints = endpointService.findByExternalEndpointIds(endpointStatusRequest.getExternalEndpointIds());
        final var mappedEndpoints = new HashMap<String, EndpointErrorsDto>();
        endpoints.forEach(endpoint -> {
            final var endpointWithStatusDto = EndpointStatusHelper.mapErrors(modelMapper, endpointService, endpoint);
            mappedEndpoints.put(endpoint.getExternalEndpointId(), endpointWithStatusDto);
        });
        return ResponseEntity.ok(new EndpointErrorsResponse(mappedEndpoints));
    }

    /**
     * Fetch the missing ACKs.
     *
     * @param endpointStatusRequest -
     * @param errors                -
     * @return -
     */
    @PostMapping(
            value = "/status/missing-acknowledgements",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "endpoint.status.missing-acknowledgements",
            description = "Fetch the missing acknowledgements of an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The status information for this endpoint.",
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
    public ResponseEntity<MissingAcknowledgementsResponse> missingAcknowledgements(@Parameter(description = "The to search for one or multiple endpoints.", required = true) @Valid @RequestBody EndpointStatusRequest endpointStatusRequest,
                                                                                   @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var endpoints = endpointService.findByExternalEndpointIds(endpointStatusRequest.getExternalEndpointIds());
        final var mappedEndpoints = new HashMap<String, MissingAcknowledgementsDto>();
        endpoints.forEach(endpoint -> {
            final var endpointWithStatusDto = EndpointStatusHelper.mapMissingAcknowledgements(modelMapper, endpointService, messageWaitingForAcknowledgementService, endpoint);
            mappedEndpoints.put(endpoint.getExternalEndpointId(), endpointWithStatusDto);
        });
        return ResponseEntity.ok(new MissingAcknowledgementsResponse(mappedEndpoints));
    }

    /**
     * Fetch the technical connection state.
     *
     * @param endpointStatusRequest -
     * @param errors                -
     * @return -
     */
    @PostMapping(
            value = "/status/technical-connection-state",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "endpoint.status.technical-connection-state",
            description = "Fetch the technical connection state of an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The status information for this endpoint.",
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
    public ResponseEntity<TechnicalConnectionStateResponse> technicalConnectionState(@Parameter(description = "The to search for one or multiple endpoints.", required = true) @Valid @RequestBody EndpointStatusRequest endpointStatusRequest,
                                                                                     @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var endpoints = endpointService.findByExternalEndpointIds(endpointStatusRequest.getExternalEndpointIds());
        final var mappedEndpoints = new HashMap<String, TechnicalConnectionStateDto>();
        endpoints.forEach(endpoint -> {
            final var technicalConnectionStateDto = EndpointStatusHelper.mapTechnicalConnectionState(modelMapper, applicationService, endpointService, mqttClientManagementService, endpoint);
            mappedEndpoints.put(endpoint.getExternalEndpointId(), technicalConnectionStateDto);
        });
        return ResponseEntity.ok(new TechnicalConnectionStateResponse(mappedEndpoints));
    }

    /**
     * Fetch the health status for an endpoint. This is the more common way to have a quick health check for the endpoint.
     *
     * @param externalEndpointId -
     * @return -
     */
    @GetMapping(
            value = "/health/{externalEndpointId}"
    )
    @Operation(
            operationId = "endpoint.health",
            description = "Fetch the health status of an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The endpoint is connected and is able to communicate."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "In case that the endpoint was not found."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of an business exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "503",
                            description = "In case the endpoint is currently not connected."
                    ),
                    @ApiResponse(
                            responseCode = "502",
                            description = "In case the endpoint is currently not able to communicate due to problems with the agrirouter."
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
    public ResponseEntity<Void> health(@Parameter(description = "The external endpoint id.", required = true) @PathVariable String externalEndpointId) {
        if (agrirouterStatusIntegrationService.isOperational()) {
            try {
                endpointService.findByExternalEndpointId(externalEndpointId);
                if (endpointService.isHealthy(externalEndpointId)) {
                    return ResponseEntity.status(HttpStatus.OK).build();
                } else {
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                }
            } catch (BusinessException e) {
                if (e.getErrorMessage().getKey().equals(ErrorKey.ENDPOINT_NOT_FOUND)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                } else {
                    throw e;
                }
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    /**
     * Fetch the health status for an endpoint. This is the more common way to have a quick health check for the endpoint.
     *
     * @return -
     */
    @PostMapping(
            value = "/health",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "endpoint.health-for-multiple",
            description = "Fetch the health status of multiple existing endpoints.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "A wrapper object with a status for each of the given IDs.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = EndpointHealthStatusResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of an business exception.",
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
    public ResponseEntity<EndpointHealthStatusResponse> health(@Parameter(description = "The external endpoint id.", required = true) @Valid @RequestBody EndpointHealthStatusRequest endpointHealthStatusRequest, @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        Map<String, Integer> endpointStatus = new HashMap<>();
        endpointHealthStatusRequest.getExternalEndpointIds().forEach(externalEndpointId -> {
            final var optionalEndpoint = endpointService.findByExternalEndpointId(externalEndpointId);
            if (optionalEndpoint.isPresent()) {
                final var endpoint = optionalEndpoint.get();
                if (endpoint.isHealthy()) {
                    endpointStatus.put(externalEndpointId, HttpStatus.OK.value());
                } else {
                    endpointStatus.put(externalEndpointId, HttpStatus.SERVICE_UNAVAILABLE.value());
                }
            } else {
                endpointStatus.put(externalEndpointId, HttpStatus.NOT_FOUND.value());
            }
        });
        return ResponseEntity.ok(new EndpointHealthStatusResponse(endpointStatus));
    }


    /**
     * Fetch the recipients for an endpoint.
     *
     * @param externalEndpointId -
     * @return -
     */
    @GetMapping(
            value = "/recipients/{externalEndpointId}"
    )
    @Operation(
            operationId = "endpoint.recipients",
            description = "Fetch the recipients of an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Response with all the recipients available.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = EndpointRecipientsResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "In case that the endpoint was not found."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of an business exception.",
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
    public ResponseEntity<EndpointRecipientsResponse> recipients(@Parameter(description = "The external endpoint id.", required = true) @PathVariable String externalEndpointId) {
        final var messageRecipientDtos = endpointService.getMessageRecipients(externalEndpointId)
                .stream()
                .map(messageRecipient -> modelMapper.map(messageRecipient, MessageRecipientDto.class))
                .toList();
        return ResponseEntity.ok(new EndpointRecipientsResponse(messageRecipientDtos));
    }

    @GetMapping(
            value = "/events/{externalEndpointId}"
    )
    @Operation(
            operationId = "endpoint.events",
            description = "Fetch the business events of an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Response with all the events currently available.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = BusinessEventsResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "In case that the endpoint was not found."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of an business exception.",
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
    public ResponseEntity<BusinessEventsResponse> events(@Parameter(description = "The external endpoint id.", required = true) @PathVariable String externalEndpointId) {
        final var businessEvents = endpointService.getBusinessEvents(externalEndpointId);
        return ResponseEntity.status(HttpStatus.OK).body(new BusinessEventsResponse(externalEndpointId, businessEvents));
    }

}
