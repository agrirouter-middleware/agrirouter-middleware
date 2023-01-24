package de.agrirouter.middleware.controller.secured;

import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.SearchNonTelemetryDataService;
import de.agrirouter.middleware.business.cache.query.LatestHeaderQueryResults;
import de.agrirouter.middleware.business.cache.query.LatestQueryResults;
import de.agrirouter.middleware.business.security.AuthorizationService;
import de.agrirouter.middleware.controller.dto.MessageStatisticsGroupedByApplicationResponse;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import de.agrirouter.middleware.controller.dto.response.LatestHeaderQueryResultsResponse;
import de.agrirouter.middleware.controller.dto.response.LatestQueryResultsResponse;
import de.agrirouter.middleware.controller.dto.response.domain.MqttStatisticsResponse;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.mqtt.MqttStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controller for statistics.
 */
@Tag(
        name = "statistics",
        description = "Statistics for the whole application."
)
@Slf4j
@RestController
@RequestMapping(SecuredApiController.API_PREFIX + "/statistics")
public class StatisticsController {

    private final MqttStatistics mqttStatistics;
    private final MqttClientManagementService mqttClientManagementService;
    private final ModelMapper modelMapper;
    private final ApplicationService applicationService;
    private final AuthorizationService authorizationService;
    private final LatestQueryResults latestQueryResults;
    private final LatestHeaderQueryResults latestHeaderQueryResults;
    private final SearchNonTelemetryDataService searchNonTelemetryDataService;

    public StatisticsController(MqttStatistics mqttStatistics,
                                MqttClientManagementService mqttClientManagementService,
                                ModelMapper modelMapper,
                                ApplicationService applicationService,
                                AuthorizationService authorizationService,
                                LatestQueryResults latestQueryResults,
                                LatestHeaderQueryResults latestHeaderQueryResults,
                                SearchNonTelemetryDataService searchNonTelemetryDataService) {
        this.mqttStatistics = mqttStatistics;
        this.mqttClientManagementService = mqttClientManagementService;
        this.modelMapper = modelMapper;
        this.applicationService = applicationService;
        this.authorizationService = authorizationService;
        this.latestQueryResults = latestQueryResults;
        this.latestHeaderQueryResults = latestHeaderQueryResults;
        this.searchNonTelemetryDataService = searchNonTelemetryDataService;
    }

    /**
     * Get the statistics for the MQTT connections.
     *
     * @return Statistics for the MQTT connections.
     */
    @GetMapping(value = "/mqtt", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "statistics.mqtt",
            description = "Get the statistics for the MQTT connections.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The statistics for the MQTT connections.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = MqttStatisticsResponse.class
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
    public ResponseEntity<MqttStatisticsResponse> getMqttStatistics() {
        var mqttStatisticsResponse = modelMapper.map(mqttStatistics, MqttStatisticsResponse.class);
        mqttStatisticsResponse.setNumberOfConnectedClients(mqttClientManagementService.getNumberOfActiveConnections());
        mqttStatisticsResponse.setNumberOfDisconnectedClients(mqttClientManagementService.getNumberOfInactiveConnections());
        return ResponseEntity.ok(mqttStatisticsResponse);
    }

    @GetMapping(value = {"/latest-query-results", "/latest-query-results/{internalApplicationId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "statistics.latest-query-results",
            description = "Get the statistics for the latest query results.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The statistics for the latest query results.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = LatestQueryResultsResponse.class
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
    public ResponseEntity<?> getLatestQueryResults(Principal principal,
                                                   @PathVariable Optional<String> internalApplicationId) {
        var latestQueryResultsResponse = new LatestQueryResultsResponse();
        final List<Application> applications;
        if (internalApplicationId.isPresent()) {
            if (authorizationService.isAuthorized(principal, internalApplicationId.get())) {
                applications = Collections.singletonList(applicationService.find(internalApplicationId.get()));
            } else {
                var errorMessage = ErrorMessageFactory.notAuthorized();
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(errorMessage.getKey().getKey(), errorMessage.getMessage()));
            }
        } else {
            applications = applicationService.findAll(principal);
        }

        applications.forEach(application -> application.getEndpoints().forEach(endpoint -> {
            var queryResult = latestQueryResults.get(endpoint.getExternalEndpointId());
            if (queryResult != null) {
                latestQueryResultsResponse.add(application.getInternalApplicationId(), endpoint.getExternalEndpointId(), queryResult);
            } else {
                log.warn("No query result found for endpoint {}", endpoint.getExternalEndpointId());
                latestQueryResultsResponse.add(application.getInternalApplicationId(), endpoint.getExternalEndpointId(), null);
            }
        }));
        return ResponseEntity.ok(latestQueryResultsResponse);
    }

    @GetMapping(value = {"/latest-header-query-results", "/latest-header-query-results/{internalApplicationId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "statistics.latest-query-results",
            description = "Get the statistics for the latest query results.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The statistics for the latest query results.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = LatestQueryResultsResponse.class
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
    public ResponseEntity<?> getLatestHeaderQueryResults(Principal principal,
                                                         @PathVariable Optional<String> internalApplicationId) {
        var latestHeaderQueryResultsResponse = new LatestHeaderQueryResultsResponse();
        final List<Application> applications;
        if (internalApplicationId.isPresent()) {
            if (authorizationService.isAuthorized(principal, internalApplicationId.get())) {
                applications = Collections.singletonList(applicationService.find(internalApplicationId.get()));
            } else {
                var errorMessage = ErrorMessageFactory.notAuthorized();
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(errorMessage.getKey().getKey(), errorMessage.getMessage()));
            }
        } else {
            applications = applicationService.findAll(principal);
        }

        applications.forEach(application -> application.getEndpoints().forEach(endpoint -> {
            var queryResult = latestHeaderQueryResults.get(endpoint.getExternalEndpointId());
            if (queryResult != null) {
                latestHeaderQueryResultsResponse.add(application.getInternalApplicationId(), endpoint.getExternalEndpointId(), queryResult);
            } else {
                log.warn("No query result found for endpoint {}", endpoint.getExternalEndpointId());
                latestHeaderQueryResultsResponse.add(application.getInternalApplicationId(), endpoint.getExternalEndpointId(), null);
            }
        }));
        return ResponseEntity.ok(latestHeaderQueryResultsResponse);
    }

    @GetMapping(value = {"/message-count", "/message-count/{internalApplicationId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMessageStatistics(Principal principal,
                                                  @PathVariable Optional<String> internalApplicationId) {
        var messageStatisticsRespose = new MessageStatisticsGroupedByApplicationResponse();
        final List<Application> applications;
        if (internalApplicationId.isPresent()) {
            if (authorizationService.isAuthorized(principal, internalApplicationId.get())) {
                applications = Collections.singletonList(applicationService.find(internalApplicationId.get()));
            } else {
                var errorMessage = ErrorMessageFactory.notAuthorized();
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(errorMessage.getKey().getKey(), errorMessage.getMessage()));
            }
        } else {
            applications = applicationService.findAll(principal);
        }

        applications.forEach(application -> application.getEndpoints().forEach(endpoint -> {
            var messageStatistics = searchNonTelemetryDataService.getMessageStatistics(endpoint.getExternalEndpointId());
            messageStatisticsRespose.add(application.getInternalApplicationId(), modelMapper.map(messageStatistics, MessageStatisticsGroupedByApplicationResponse.MessageStatisticGroupedBySender.class));
        }));

        return ResponseEntity.ok(messageStatisticsRespose);
    }

}
