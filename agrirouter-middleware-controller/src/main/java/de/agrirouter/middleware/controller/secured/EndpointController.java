package de.agrirouter.middleware.controller.secured;

import de.agrirouter.middleware.api.errorhandling.ParameterValidationException;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.controller.dto.request.EndpointStatusRequest;
import de.agrirouter.middleware.controller.dto.response.EndpointStatusResponse;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import de.agrirouter.middleware.controller.dto.response.ParameterValidationProblemResponse;
import de.agrirouter.middleware.controller.dto.response.domain.EndpointWithStatusDto;
import de.agrirouter.middleware.controller.helper.CreateEndpointStatusHelper;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

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
            produces = MediaType.APPLICATION_JSON_VALUE
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
    public ResponseEntity<EndpointStatusResponse> status(@Parameter(description = "The to search for one or multiple endpoints.", required = true) @RequestBody EndpointStatusRequest endpointStatusRequest,
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

}
