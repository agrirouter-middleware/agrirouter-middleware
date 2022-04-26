package de.agrirouter.middleware.controller.secured;

import de.agrirouter.middleware.api.errorhandling.ParameterValidationException;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.controller.dto.request.EndpointHealthStatusRequest;
import de.agrirouter.middleware.controller.dto.request.EndpointStatusRequest;
import de.agrirouter.middleware.controller.dto.response.*;
import de.agrirouter.middleware.controller.dto.response.domain.EndpointWithStatusDto;
import de.agrirouter.middleware.controller.dto.response.domain.MessageRecipientDto;
import de.agrirouter.middleware.controller.helper.CreateEndpointStatusHelper;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
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

    public EndpointController(ApplicationService applicationService,
                              EndpointService endpointService,
                              ModelMapper modelMapper,
                              MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService) {
        this.applicationService = applicationService;
        this.endpointService = endpointService;
        this.modelMapper = modelMapper;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
    }

    /**
     * Find an endpoint status of an application by the given IDs of the endpoint.
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
            final var endpointWithStatusDto = CreateEndpointStatusHelper.map(modelMapper, endpointService, applicationService, messageWaitingForAcknowledgementService, endpoint);
            mappedEndpoints.put(endpoint.getExternalEndpointId(), endpointWithStatusDto);
        });
        return ResponseEntity.ok(new EndpointStatusResponse(mappedEndpoints));
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
                            responseCode = "500",
                            description = "In case of an unknown error.",
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
                    )
            }
    )
    public ResponseEntity<Void> health(@Parameter(description = "The external endpoint id.", required = true) @PathVariable String externalEndpointId) {
        final var optionalEndpoint = endpointService.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            if (endpoint.isHealthy()) {
                return ResponseEntity.status(HttpStatus.OK).build();
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
                            description = "Reponse with all the recipients available.",
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
        final var optionalEndpoint = endpointService.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            final var messageRecipientDtos = endpoint.getMessageRecipients()
                    .stream()
                    .map(messageRecipient -> modelMapper.map(messageRecipient, MessageRecipientDto.class))
                    .toList();
            return ResponseEntity.ok(new EndpointRecipientsResponse(messageRecipientDtos));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
