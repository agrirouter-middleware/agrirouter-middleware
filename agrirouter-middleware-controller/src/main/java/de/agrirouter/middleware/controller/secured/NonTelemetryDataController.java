package de.agrirouter.middleware.controller.secured;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import de.agrirouter.middleware.api.errorhandling.ParameterValidationException;
import de.agrirouter.middleware.business.PublishNonTelemetryDataService;
import de.agrirouter.middleware.business.SearchNonTelemetryDataService;
import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;
import de.agrirouter.middleware.business.parameters.SearchNonTelemetryDataParameters;
import de.agrirouter.middleware.controller.dto.request.SearchFilesRequest;
import de.agrirouter.middleware.controller.dto.request.messaging.PublishImageDataRequest;
import de.agrirouter.middleware.controller.dto.request.messaging.PublishNonTelemetryDataRequest;
import de.agrirouter.middleware.controller.dto.request.messaging.PublishVideoDataRequest;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import de.agrirouter.middleware.controller.dto.response.ParameterValidationProblemResponse;
import de.agrirouter.middleware.controller.dto.response.SearchFilesResponse;
import de.agrirouter.middleware.controller.dto.response.domain.FileHeaderDto;
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

/**
 * Sending messages to the AR.
 */
@RestController
@RequestMapping(SecuredApiController.API_PREFIX + "/non-telemetry-data")
@Tag(
        name = "non telemetry data management",
        description = "Operations for non telemetry data management, i.e. publish images, task data or documents to the agrirouter."
)
public class NonTelemetryDataController implements SecuredApiController {

    private final PublishNonTelemetryDataService publishNonTelemetryDataService;
    private final SearchNonTelemetryDataService searchNonTelemetryDataService;
    private final ModelMapper modelMapper;

    public NonTelemetryDataController(PublishNonTelemetryDataService publishNonTelemetryDataService,
                                      SearchNonTelemetryDataService searchNonTelemetryDataService,
                                      ModelMapper modelMapper) {
        this.publishNonTelemetryDataService = publishNonTelemetryDataService;
        this.searchNonTelemetryDataService = searchNonTelemetryDataService;
        this.modelMapper = modelMapper;
    }

    /**
     * Publish task data files for a given endpoint.
     *
     * @param publishNonTelemetryDataRequest -
     * @return -
     */
    @PostMapping(
            value = "/publish/taskdata/{externalEndpointId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "non-telemetry-data.publish-taskdata",
            description = "Publish a task data file for an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Response if the message was published successfully.",
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
    public ResponseEntity<Void> publishTaskData(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId,
                                                @Parameter(description = "The request body containing all necessary information to publish task data files.", required = true) @Valid @RequestBody PublishNonTelemetryDataRequest publishNonTelemetryDataRequest,
                                                @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        publishNonTelemetryDataRequest.getMessageTuples().forEach(messageTuple -> {
                    final var publishNonTelemetryDataParameters = new PublishNonTelemetryDataParameters();
                    publishNonTelemetryDataParameters.setExternalEndpointId(externalEndpointId);
                    publishNonTelemetryDataParameters.setBase64EncodedMessageContent(messageTuple.getMessageContent());
                    publishNonTelemetryDataParameters.setFilename(messageTuple.getFileName());
                    publishNonTelemetryDataParameters.setRecipients(messageTuple.getRecipients());
                    publishNonTelemetryDataParameters.setContentMessageType(ContentMessageType.ISO_11783_TASKDATA_ZIP);
                    publishNonTelemetryDataService.publish(publishNonTelemetryDataParameters);
                }
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Publish shape files for a given endpoint.
     *
     * @param publishNonTelemetryDataRequest -
     * @return -
     */
    @PostMapping(
            value = "/publish/shape/{externalEndpointId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "non-telemetry-data.publish-shape",
            description = "Publish a shape file for an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Response if the message was published successfully.",
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
    public ResponseEntity<Void> publishShape(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId,
                                             @Parameter(description = "The request body containing all necessary information to publish shape files.", required = true) @Valid @RequestBody PublishNonTelemetryDataRequest publishNonTelemetryDataRequest,
                                             @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        publishNonTelemetryDataRequest.getMessageTuples().forEach(messageTuple -> {
                    final var publishNonTelemetryDataParameters = new PublishNonTelemetryDataParameters();
                    publishNonTelemetryDataParameters.setExternalEndpointId(externalEndpointId);
                    publishNonTelemetryDataParameters.setBase64EncodedMessageContent(messageTuple.getMessageContent());
                    publishNonTelemetryDataParameters.setFilename(messageTuple.getFileName());
                    publishNonTelemetryDataParameters.setRecipients(messageTuple.getRecipients());
                    publishNonTelemetryDataParameters.setContentMessageType(ContentMessageType.SHP_SHAPE_ZIP);
                    publishNonTelemetryDataService.publish(publishNonTelemetryDataParameters);
                }
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Publish images for a given endpoint.
     *
     * @param publishImageDataRequest -
     * @return -
     */
    @PostMapping(
            value = "/publish/image/{externalEndpointId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "non-telemetry-data.publish-image",
            description = "Publish image files for an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Response if the message was published successfully.",
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
    public ResponseEntity<Void> publishImage(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId,
                                             @Parameter(description = "The request body containing all necessary information to publish image files.", required = true) @Valid @RequestBody PublishImageDataRequest publishImageDataRequest,
                                             @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        publishImageDataRequest.getMessageTuples().forEach(messageTuple -> {
                    final var publishNonTelemetryDataParameters = new PublishNonTelemetryDataParameters();
                    publishNonTelemetryDataParameters.setExternalEndpointId(externalEndpointId);
                    publishNonTelemetryDataParameters.setBase64EncodedMessageContent(messageTuple.getMessageContent());
                    publishNonTelemetryDataParameters.setFilename(messageTuple.getFileName());
                    publishNonTelemetryDataParameters.setRecipients(messageTuple.getRecipients());
                    publishNonTelemetryDataParameters.setContentMessageType(publishImageDataRequest.getImageType().getContentMessageType());
                    publishNonTelemetryDataService.publish(publishNonTelemetryDataParameters);
                }
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Publish videos for a given endpoint.
     *
     * @param publishVideoDataRequest -
     * @return -
     */
    @PostMapping(
            value = "/publish/video/{externalEndpointId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "non-telemetry-data.publish-video",
            description = "Publish video files for an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Response if the message was published successfully.",
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
    public ResponseEntity<Void> publishVideo(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId,
                                             @Parameter(description = "The request body containing all necessary information to publish video files.", required = true) @Valid @RequestBody PublishVideoDataRequest publishVideoDataRequest,
                                             @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        publishVideoDataRequest.getMessageTuples().forEach(messageTuple -> {
                    final var publishNonTelemetryDataParameters = new PublishNonTelemetryDataParameters();
                    publishNonTelemetryDataParameters.setExternalEndpointId(externalEndpointId);
                    publishNonTelemetryDataParameters.setBase64EncodedMessageContent(messageTuple.getMessageContent());
                    publishNonTelemetryDataParameters.setFilename(messageTuple.getFileName());
                    publishNonTelemetryDataParameters.setRecipients(messageTuple.getRecipients());
                    publishNonTelemetryDataParameters.setContentMessageType(publishVideoDataRequest.getVideoType().getContentMessageType());
                    publishNonTelemetryDataService.publish(publishNonTelemetryDataParameters);
                }
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Publish documents for a given endpoint.
     *
     * @param publishNonTelemetryDataRequest -
     * @return -
     */
    @PostMapping(
            value = "/publish/pdf/{externalEndpointId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "non-telemetry-data.publish-PDF",
            description = "Publish PDF files for an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Response if the message was published successfully.",
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
    public ResponseEntity<Void> publishPdf(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId,
                                           @Parameter(description = "The request body containing all necessary information to publish PDF files.", required = true) @Valid @RequestBody PublishNonTelemetryDataRequest publishNonTelemetryDataRequest,
                                           @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        publishNonTelemetryDataRequest.getMessageTuples().forEach(messageTuple -> {
                    final var publishNonTelemetryDataParameters = new PublishNonTelemetryDataParameters();
                    publishNonTelemetryDataParameters.setExternalEndpointId(externalEndpointId);
                    publishNonTelemetryDataParameters.setBase64EncodedMessageContent(messageTuple.getMessageContent());
                    publishNonTelemetryDataParameters.setFilename(messageTuple.getFileName());
                    publishNonTelemetryDataParameters.setRecipients(messageTuple.getRecipients());
                    publishNonTelemetryDataParameters.setContentMessageType(ContentMessageType.DOC_PDF);
                    publishNonTelemetryDataService.publish(publishNonTelemetryDataParameters);
                }
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Publish documents for a given endpoint.
     *
     * @param externalEndpointId -
     * @param searchFilesRequest -
     * @param errors             -
     * @return -
     */
    @PostMapping(
            value = "/search/{externalEndpointId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "non-telemetry-data.search-headers",
            description = "Search for file headers for an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Response containing the results for the search.",
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
    public ResponseEntity<SearchFilesResponse> search(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId,
                                                      @Parameter(description = "The request body containing all necessary information to search for file headers.", required = true) @Valid @RequestBody SearchFilesRequest searchFilesRequest,
                                                      @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var searchNonTelemetryDataParameters = modelMapper.map(searchFilesRequest, SearchNonTelemetryDataParameters.class);
        searchNonTelemetryDataParameters.setExternalEndpointId(externalEndpointId);
        final var contentMessageMetadata = searchNonTelemetryDataService.search(searchNonTelemetryDataParameters);
        final var fileHeaderDtos = contentMessageMetadata.stream().map(cmm -> modelMapper.map(cmm, FileHeaderDto.class)).toList();
        final var searchFilesResponse = new SearchFilesResponse(fileHeaderDtos.size(), fileHeaderDtos);
        return ResponseEntity.ok(searchFilesResponse);
    }

    /**
     * Download message content from a content message. This only applies to "non-telemetry-data".
     *
     * @param externalEndpointId -
     * @param messageId          -
     * @return -
     */
    @GetMapping(
            value = "/download/{externalEndpointId}/{messageId}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @Operation(
            operationId = "non-telemetry-data.download",
            description = "Download a file for an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Response containing the result.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE
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
    public @ResponseBody
    byte[] download(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId,
                    @Parameter(description = "The ID of the message.", required = true) @PathVariable String messageId) {
        return searchNonTelemetryDataService.downloadAsByteArray(externalEndpointId, messageId);
    }

    /**
     * Download message content from a content message. This only applies to "non-telemetry-data".
     *
     * @param externalEndpointId -
     * @param messageId          -
     * @return -
     */
    @DeleteMapping(
            value = "/{externalEndpointId}/{messageId}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @Operation(
            operationId = "non-telemetry-data.delete",
            description = "Delete a file for an existing endpoint.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The file was deleted.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE
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
    public ResponseEntity<Void> delete(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId,
                                @Parameter(description = "The ID of the message.", required = true) @PathVariable String messageId) {
        searchNonTelemetryDataService.delete(externalEndpointId, messageId);
        return ResponseEntity.ok().build();
    }

}