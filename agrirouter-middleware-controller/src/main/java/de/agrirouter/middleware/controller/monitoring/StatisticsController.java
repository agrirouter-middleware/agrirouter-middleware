package de.agrirouter.middleware.controller.monitoring;

import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.SearchNonTelemetryDataService;
import de.agrirouter.middleware.business.cache.query.LatestHeaderQueryResults;
import de.agrirouter.middleware.business.cache.query.LatestQueryResults;
import de.agrirouter.middleware.business.security.AuthorizationService;
import de.agrirouter.middleware.controller.MonitoringApiController;
import de.agrirouter.middleware.controller.SecuredApiController;
import de.agrirouter.middleware.controller.dto.MessageStatisticsGroupedByApplicationResponse;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import de.agrirouter.middleware.controller.dto.response.LatestHeaderQueryResultsResponse;
import de.agrirouter.middleware.controller.dto.response.LatestQueryResultsResponse;
import de.agrirouter.middleware.domain.Application;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@Slf4j
@RestController
@Tag(name = "monitoring")
@RequiredArgsConstructor
@RequestMapping(MonitoringApiController.API_PREFIX + "/statistics")
public class StatisticsController implements SecuredApiController {

    private final ModelMapper modelMapper;
    private final ApplicationService applicationService;
    private final AuthorizationService authorizationService;
    private final LatestQueryResults latestQueryResults;
    private final LatestHeaderQueryResults latestHeaderQueryResults;
    private final SearchNonTelemetryDataService searchNonTelemetryDataService;

    @GetMapping(value = {"/latest-query-results", "/latest-query-results/{internalApplicationId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(hidden = true)
    public ResponseEntity<?> getLatestQueryResults(Principal principal,
                                                             @PathVariable Optional<String> internalApplicationId) {
        var latestQueryResultsResponse = new LatestQueryResultsResponse();
        final List<Application> applications;
        if (internalApplicationId.isPresent()) {
            if (authorizationService.isAuthorized(principal, internalApplicationId.get())) {
                applications = Collections.singletonList(applicationService.find(internalApplicationId.get()));
            } else {
                var errorMessage = ErrorMessageFactory.notAuthorized();
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(errorMessage.key().getKey(), errorMessage.message()));
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
    @Operation(hidden = true)
    public ResponseEntity<?> getLatestHeaderQueryResults(Principal principal,
                                                                   @PathVariable Optional<String> internalApplicationId) {
        var latestHeaderQueryResultsResponse = new LatestHeaderQueryResultsResponse();
        final List<Application> applications;
        if (internalApplicationId.isPresent()) {
            if (authorizationService.isAuthorized(principal, internalApplicationId.get())) {
                applications = Collections.singletonList(applicationService.find(internalApplicationId.get()));
            } else {
                var errorMessage = ErrorMessageFactory.notAuthorized();
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(errorMessage.key().getKey(), errorMessage.message()));
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
    @Operation(hidden = true)
    public ResponseEntity<?> getMessageStatistics(Principal principal,
                                                            @PathVariable Optional<String> internalApplicationId) {
        var messageStatisticsRespose = new MessageStatisticsGroupedByApplicationResponse();
        final List<Application> applications;
        if (internalApplicationId.isPresent()) {
            if (authorizationService.isAuthorized(principal, internalApplicationId.get())) {
                applications = Collections.singletonList(applicationService.find(internalApplicationId.get()));
            } else {
                var errorMessage = ErrorMessageFactory.notAuthorized();
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(errorMessage.key().getKey(), errorMessage.message()));
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
