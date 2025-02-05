package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.cache.endpoints.InternalEndpointCache;
import de.agrirouter.middleware.business.cache.events.BusinessEventType;
import de.agrirouter.middleware.business.cache.events.BusinessEventsCache;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.enums.EndpointType;
import de.agrirouter.middleware.domain.log.Error;
import de.agrirouter.middleware.domain.log.Warning;
import de.agrirouter.middleware.integration.EndpointIntegrationService;
import de.agrirouter.middleware.integration.RevokeProcessIntegrationService;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.ConnectionState;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.mqtt.health.HealthStatus;
import de.agrirouter.middleware.integration.mqtt.health.HealthStatusIntegrationService;
import de.agrirouter.middleware.integration.mqtt.health.HealthStatusWithLastKnownHealthyStatus;
import de.agrirouter.middleware.integration.mqtt.list_endpoints.ListEndpointsIntegrationService;
import de.agrirouter.middleware.integration.mqtt.list_endpoints.MessageRecipient;
import de.agrirouter.middleware.integration.mqtt.list_endpoints.cache.MessageRecipientCache;
import de.agrirouter.middleware.persistence.jpa.ApplicationRepository;
import de.agrirouter.middleware.persistence.jpa.EndpointRepository;
import de.agrirouter.middleware.persistence.jpa.ErrorRepository;
import de.agrirouter.middleware.persistence.jpa.WarningRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/**
 * Business operations regarding the endpoints.
 *
 */
@Slf4j
@Service
public class EndpointService {

    private final EndpointRepository endpointRepository;
    private final DecodeMessageService decodeMessageService;
    private final ErrorRepository errorRepository;
    private final WarningRepository warningRepository;
    private final EndpointIntegrationService endpointIntegrationService;
    private final ApplicationRepository applicationRepository;
    private final MqttClientManagementService mqttClientManagementService;
    private final HealthStatusIntegrationService healthStatusIntegrationService;
    private final RevokeProcessIntegrationService revokeProcessIntegrationService;
    private final BusinessOperationLogService businessOperationLogService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final BusinessEventsCache businessEventsCache;
    private final ListEndpointsIntegrationService listEndpointsIntegrationService;
    private final MessageRecipientCache messageRecipientCache;
    private final InternalEndpointCache internalEndpointCache;
    private final RemoveEndpointDataService removeEndpointDataService;

    @Value("${app.agrirouter.mqtt.synchronous.response.wait.time}")
    private int nrOfMillisecondsToWaitForTheResponseOfTheAgrirouter;

    @Value("${app.agrirouter.mqtt.synchronous.health.response.wait.time}")
    private int nrOfMillisecondsToWaitForTheHealthResponseOfTheAgrirouter;

    @Value("${app.agrirouter.mqtt.synchronous.response.polling.intervall}")
    private int pollingInterval;

    @Value("${app.agrirouter.threading.fixed-thread-pool-size}")
    private int fixedThreadPoolSize;

    public EndpointService(EndpointRepository endpointRepository,
                           DecodeMessageService decodeMessageService,
                           ErrorRepository errorRepository,
                           WarningRepository warningRepository,
                           EndpointIntegrationService endpointIntegrationService,
                           ApplicationRepository applicationRepository,
                           MqttClientManagementService mqttClientManagementService,
                           HealthStatusIntegrationService healthStatusIntegrationService,
                           RevokeProcessIntegrationService revokeProcessIntegrationService,
                           BusinessOperationLogService businessOperationLogService,
                           MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                           BusinessEventsCache businessEventsCache,
                           ListEndpointsIntegrationService listEndpointsIntegrationService,
                           MessageRecipientCache messageRecipientCache,
                           InternalEndpointCache internalEndpointCache,
                           RemoveEndpointDataService removeEndpointDataService) {
        this.endpointRepository = endpointRepository;
        this.decodeMessageService = decodeMessageService;
        this.errorRepository = errorRepository;
        this.warningRepository = warningRepository;
        this.endpointIntegrationService = endpointIntegrationService;
        this.applicationRepository = applicationRepository;
        this.mqttClientManagementService = mqttClientManagementService;
        this.healthStatusIntegrationService = healthStatusIntegrationService;
        this.revokeProcessIntegrationService = revokeProcessIntegrationService;
        this.businessOperationLogService = businessOperationLogService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.businessEventsCache = businessEventsCache;
        this.listEndpointsIntegrationService = listEndpointsIntegrationService;
        this.messageRecipientCache = messageRecipientCache;
        this.internalEndpointCache = internalEndpointCache;
        this.removeEndpointDataService = removeEndpointDataService;
    }

    /**
     * Updating error messages based on the decoded message.
     *
     * @param endpoint       -
     * @param decodedMessage -
     */
    public void updateErrors(Endpoint endpoint, DecodeMessageResponse decodedMessage) {
        final var messages = decodeMessageService.decode(decodedMessage.getResponsePayloadWrapper().getDetails());
        final var message = messages.getMessages(0);
        log.debug("Update status of the endpoint.");
        final var error = new Error();
        error.setResponseCode(decodedMessage.getResponseEnvelope().getResponseCode());
        error.setResponseType(decodedMessage.getResponseEnvelope().getType().name());
        error.setMessageId(decodedMessage.getResponseEnvelope().getMessageId());
        error.setTimestamp(decodedMessage.getResponseEnvelope().getTimestamp().getSeconds());
        error.setMessage(String.format("[%s] %s", message.getMessageCode(), message.getMessage()));
        error.setEndpoint(endpoint);
        errorRepository.save(error);
        businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Error has been created.");
    }

    /**
     * Updating warning messages based on the decoded message.
     *
     * @param endpoint       -
     * @param decodedMessage -
     */
    public void updateWarnings(Endpoint endpoint, DecodeMessageResponse decodedMessage) {
        final var messages = decodeMessageService.decode(decodedMessage.getResponsePayloadWrapper().getDetails());
        final var message = messages.getMessages(0);
        log.debug("Update status of the endpoint.");
        final var warning = new Warning();
        warning.setResponseCode(decodedMessage.getResponseEnvelope().getResponseCode());
        warning.setResponseType(decodedMessage.getResponseEnvelope().getType().name());
        warning.setMessageId(decodedMessage.getResponseEnvelope().getMessageId());
        warning.setTimestamp(decodedMessage.getResponseEnvelope().getTimestamp().getSeconds());
        warning.setMessage(String.format("[%s] %s", message.getMessageCode(), message.getMessage()));
        warning.setEndpoint(endpoint);
        warningRepository.save(warning);
        businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Warning has been created.");
    }

    /**
     * Delete the endpoint and remove all the data.
     *
     * @param agrirouterEndpointId -
     */
    @Async
    @Transactional
    public void deleteEndpointDataFromTheMiddlewareByAgrirouterId(String agrirouterEndpointId) {
        final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(agrirouterEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            delete(optionalEndpoint.get().getExternalEndpointId());
            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Endpoint data incl. the endpoint was deleted.");
        } else {
            log.warn("Endpoint with agrirouter endpoint ID {} not found.", agrirouterEndpointId);
        }
    }

    /**
     * Delete the endpoint and remove all the data.
     *
     * @param externalEndpointId The external endpoint ID.
     */
    @Transactional
    public void delete(String externalEndpointId) {
        var endpoints = endpointRepository.findAllByExternalEndpointId(externalEndpointId);
        var sensorAlternateIds = new ArrayList<String>();
        var mainExecutorService = Executors.newFixedThreadPool(endpoints.size());
        endpoints.forEach(endpoint -> mainExecutorService.execute(() -> {
            try {
                List<Endpoint> connectedVirtualEndpoints = endpoint.getConnectedVirtualEndpoints();
                if (!CollectionUtils.isEmpty(connectedVirtualEndpoints)) {
                    var subExecutorService = Executors.newFixedThreadPool(connectedVirtualEndpoints.size());
                    connectedVirtualEndpoints.forEach(virtualEndpoint -> subExecutorService.execute(() -> {
                        log.debug("Remove the virtual endpoint '{}' from the database.", virtualEndpoint.getExternalEndpointId());
                        removeEndpointDataService.removeEndpointData(virtualEndpoint);
                        sensorAlternateIds.add(virtualEndpoint.getAgrirouterEndpointId());
                    }));
                    subExecutorService.shutdown();
                    boolean hasFinishedInTime = subExecutorService.awaitTermination(3, TimeUnit.MINUTES);
                    if (!hasFinishedInTime) {
                        log.error("Could not wait for the executor service to finish. The main endpoint '{}' will not be removed from the database.", endpoint.getExternalEndpointId());
                    } else {
                        log.debug("Remove the main endpoint '{}' from the database.", endpoint.getExternalEndpointId());
                        removeEndpointDataService.removeEndpointDataAndEndpoint(endpoint);
                        sensorAlternateIds.add(endpoint.getAgrirouterEndpointId());
                    }
                    log.debug("Wait for the executor service to finish.");
                } else {
                    log.debug("No connected virtual endpoints found, therefore the endpoint will be removed directly.");
                    log.debug("Remove the main endpoint '{}' from the database.", endpoint.getExternalEndpointId());
                    removeEndpointDataService.removeEndpointDataAndEndpoint(endpoint);
                    sensorAlternateIds.add(endpoint.getAgrirouterEndpointId());
                }
            } catch (InterruptedException e) {
                log.error("Could not wait for the executor service to finish.", e);
            }
        }));

        log.debug("Remove the data for the endpoint incl. messages, timelogs and so on.");
        sensorAlternateIds.forEach(removeEndpointDataService::removeData);
    }


    /**
     * Resending the capabilities.
     *
     * @param externalEndpointId The internal ID of the endpoint.
     */
    @Async
    public void resendCapabilities(String externalEndpointId) {
        final var endpoint = findByExternalEndpointId(externalEndpointId);
        final var optionalApplication = applicationRepository.findByEndpointsContains(endpoint);
        if (optionalApplication.isPresent()) {
            sendCapabilities(optionalApplication.get(), endpoint);
            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Capabilities were resent.");
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
        }
    }

    /**
     * Sending the capabilties for an endpoint.
     *
     * @param application The application defining the capabilities.
     * @param endpoint    The endpoint.
     */
    public void sendCapabilities(Application application, Endpoint endpoint) {
        endpointIntegrationService.sendCapabilities(application, endpoint);
        businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Capabilities were sent.");
    }

    /**
     * Fetch all errors for the endpoint.
     *
     * @param endpoint -
     * @return -
     */
    public List<Error> getErrors(Endpoint endpoint) {
        return errorRepository.findByEndpoint(endpoint);
    }

    /**
     * Fetch all warnings for the endpoint.
     *
     * @param endpoint -
     * @return -
     */
    public List<Warning> getWarnings(Endpoint endpoint) {
        return warningRepository.findByEndpoint(endpoint);
    }

    /**
     * Get the state of the connection.
     *
     * @param endpoint -
     * @return -
     */
    public ConnectionState getConnectionState(Endpoint endpoint) {
        return mqttClientManagementService.getState(endpoint);
    }

    /**
     * Find the endpoints by their external ID.
     *
     * @param externalEndpointIds -
     * @return -
     */
    public List<Endpoint> findByExternalEndpointIds(List<String> externalEndpointIds) {
        var endpoints = new ArrayList<Endpoint>();
        externalEndpointIds.forEach(externalEndpointId -> {
            try {
                endpoints.add(findByExternalEndpointId(externalEndpointId));
            } catch (BusinessException e) {
                log.warn("Could not find endpoint, therefore skipping the ID: " + externalEndpointId);
            }
        });
        return endpoints;
    }

    /**
     * Check if the endpoint already exists.
     *
     * @param externalId The external ID.
     * @return True if the endpoint already exists.
     */
    public boolean existsByExternalEndpointId(String externalId) {
        return endpointRepository.existsByExternalEndpointId(externalId);
    }

    /**
     * Find the endpoint by its external ID.
     *
     * @param externalEndpointId -
     * @return -
     */
    public Endpoint findByExternalEndpointId(String externalEndpointId) {
        var optionalEndpoint = internalEndpointCache.get(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            log.info("Cache hit, looks like we already requested the endpoint earlier.");
            return optionalEndpoint.get();
        } else {
            log.info("Endpoint was not cached yet, fetching the endpoint from the database and place it into the cache.");
            optionalEndpoint = endpointRepository.findByExternalEndpointId(externalEndpointId);
            if (optionalEndpoint.isPresent()) {
                internalEndpointCache.put(externalEndpointId, optionalEndpoint.get());
                return optionalEndpoint.get();
            } else {
                throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint(externalEndpointId));
            }
        }
    }

    /**
     * Revoke an endpoint.
     *
     * @param externalEndpointId -
     */
    @Async
    @Transactional
    public void revoke(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            log.debug("Deactivate the endpoint to avoid race conditions.");
            endpoint.setDeactivated(true);
            endpointRepository.save(endpoint);
            mqttClientManagementService.disconnect(endpoint);
            endpoint.getConnectedVirtualEndpoints().forEach(vcu -> {
                vcu.setDeactivated(true);
                endpointRepository.save(vcu);
                mqttClientManagementService.disconnect(vcu);
            });
            if (EndpointType.NON_VIRTUAL.equals(endpoint.getEndpointType())) {
                final var optionalApplication = applicationRepository.findByEndpointsContains(endpoint);
                if (optionalApplication.isPresent()) {
                    final var application = optionalApplication.get();
                    revokeProcessIntegrationService.revoke(application, endpoint);
                    businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Endpoint was revoked.");
                    delete(externalEndpointId);
                } else {
                    throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
                }
            } else {
                log.warn("Tried to revoke a virtual endpoint with the ID '{}'. This is not possible.", externalEndpointId);
            }
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint(externalEndpointId));
        }
    }

    /**
     * Fetch all endpoints.
     *
     * @param internalApplicationId The internal ID of the application.
     * @return The endpoints.
     */
    public List<Endpoint> findAll(String internalApplicationId) {
        return endpointRepository.findAllByInternalApplicationId(internalApplicationId);
    }

    /**
     * Reset all errors.
     *
     * @param externalEndpointId The external ID of the endpoint.
     */
    @Async
    @Transactional
    public void resetErrors(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            errorRepository.deleteAllByEndpoint(endpoint);
            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Errors were reset.");
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint(externalEndpointId));
        }
    }

    /**
     * Reset all warnings.
     *
     * @param externalEndpointId The external ID of the endpoint.
     */
    @Async
    @Transactional
    public void resetWarnings(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            warningRepository.deleteAllByEndpoint(endpoint);
            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Warnings were reset.");
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint(externalEndpointId));
        }
    }

    /**
     * Remove all messages waiting for ACK for the endpoint.
     *
     * @param externalEndpointId The external ID of the endpoint.
     */
    public void resetMessagesWaitingForAcknowledgement(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            messageWaitingForAcknowledgementService.deleteAllForEndpoint(endpoint);
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint(externalEndpointId));
        }
    }

    /**
     * Remove all connection errors.
     *
     * @param externalEndpointId The external ID of the endpoint.
     */
    public void resetConnectionErrors(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            mqttClientManagementService.clearConnectionErrors(endpoint);
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint(externalEndpointId));
        }
    }

    /**
     * Get the business events for the endpoint.
     *
     * @param externalEndpointId The external ID of the endpoint.
     * @return The business events.
     */
    public Map<BusinessEventType, Instant> getBusinessEvents(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            var optionalBusinessEvents = businessEventsCache.get(endpoint.getExternalEndpointId());
            Map<BusinessEventType, Instant> businessEvents = new HashMap<>();
            optionalBusinessEvents.ifPresent(businessEvents::putAll);
            return businessEvents;
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint(externalEndpointId));
        }
    }

    /**
     * Check whether the endpoint is healthy or not. The method will publish a message on
     *
     * @param externalEndpointId The external ID of the endpoint.
     * @return The health status.
     */
    public HealthStatusWithLastKnownHealthyStatus determineHealthStatus(String externalEndpointId) {
        final var endpoint = findByExternalEndpointId(externalEndpointId);
        healthStatusIntegrationService.publishHealthStatusMessage(endpoint);
        var healthStatus = HealthStatus.PENDING;
        var timer = nrOfMillisecondsToWaitForTheHealthResponseOfTheAgrirouter;
        while (timer > 0) {
            try {
                Thread.sleep(pollingInterval);
                healthStatus = healthStatusIntegrationService.determineHealthStatus(endpoint.getAgrirouterEndpointId());
                if (!HealthStatus.PENDING.equals(healthStatus) && !HealthStatus.UNKNOWN.equals(healthStatus)) {
                    break;
                }
            } catch (InterruptedException e) {
                log.error("Error while waiting for health status response.", e);
            }
            timer = timer - pollingInterval;
        }
        var lastKnownHealthyStatus = healthStatusIntegrationService.getLastKnownHealthyStatus(endpoint.getAgrirouterEndpointId());
        if (lastKnownHealthyStatus.isPresent()) {
            log.debug("Last known healthy status for endpoint '{}': {}.", externalEndpointId, lastKnownHealthyStatus.get());
            return new HealthStatusWithLastKnownHealthyStatus(healthStatus, lastKnownHealthyStatus.get());
        } else {
            log.debug("No last known healthy status found for endpoint '{}'.", externalEndpointId);
            return new HealthStatusWithLastKnownHealthyStatus(healthStatus, null);
        }
    }

    /**
     * Get the recipients for the endpoint.
     *
     * @return The recipients for the endpoint.
     */
    public Collection<MessageRecipient> getMessageRecipients(String externalEndpointId) {
        final var endpoint = findByExternalEndpointId(externalEndpointId);
        listEndpointsIntegrationService.publishListEndpointsMessage(endpoint);
        if (listEndpointsIntegrationService.hasPendingResponse(endpoint.getAgrirouterEndpointId())) {
            var timer = nrOfMillisecondsToWaitForTheResponseOfTheAgrirouter;
            while (timer > 0) {
                try {
                    Thread.sleep(pollingInterval);
                    var recipients = listEndpointsIntegrationService.getRecipients(endpoint.getAgrirouterEndpointId());
                    if (recipients.isPresent()) {
                        log.debug("Found recipients for endpoint {}.", endpoint.getAgrirouterEndpointId());
                        Collection<MessageRecipient> messageRecipients = recipients.get();
                        log.debug("Recipients: {}.", messageRecipients);
                        messageRecipientCache.put(endpoint.getExternalEndpointId(), messageRecipients);
                        return messageRecipients;
                    }
                } catch (InterruptedException e) {
                    log.error("Error while waiting for list endpoints / message recipients response.", e);
                }
                timer = timer - pollingInterval;
            }
        } else {
            log.debug("There is no pending list endpoints response for endpoint {}.", endpoint.getAgrirouterEndpointId());
        }
        log.debug("Could not find recipients for endpoint '{}', now checking the cache.", externalEndpointId);
        var optionalMessageRecipients = messageRecipientCache.get(externalEndpointId);
        return optionalMessageRecipients.orElse(Collections.emptyList());
    }

    /**
     * Find an endpoint by agrirouter endpoint ID.
     *
     * @param agrirouterEndpointId The agrirouter endpoint ID.
     * @return The endpoint.
     */
    public Endpoint findByAgrirouterEndpointId(String agrirouterEndpointId) {
        var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(agrirouterEndpointId);
        if (optionalEndpoint.isPresent()) {
            return optionalEndpoint.get();
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpointByAgrirouterId((agrirouterEndpointId)));
        }
    }

    /**
     * Save endpoint and update the cache.
     *
     * @param endpoint The endpoint.
     * @return The endpoint.
     */
    public Endpoint save(Endpoint endpoint) {
        var savedEndpoint = endpointRepository.save(endpoint);
        internalEndpointCache.put(savedEndpoint.getExternalEndpointId(), savedEndpoint);
        return savedEndpoint;
    }

    /**
     * Find all endpoints and place them in the cache.
     *
     * @return The endpoints.
     */
    public List<Endpoint> findAll() {
        var endpoints = endpointRepository.findAll();
        endpoints.forEach(endpoint -> internalEndpointCache.put(endpoint.getExternalEndpointId(), endpoint));
        return endpoints;
    }

    public Map<String, Integer> areHealthy(List<String> externalEndpointIds) {
        Map<String, Integer> endpointStatus = new HashMap<>();
        try (ExecutorService executorService = Executors.newFixedThreadPool(fixedThreadPoolSize)) {
            var callables = new ArrayList<Callable<TaskResult>>();
            externalEndpointIds.forEach(externalEndpointId -> callables.add(createHealthCheckTask(externalEndpointId)));
            var futures = executorService.invokeAll(callables);
            waitUntilAllTasksAreDone(futures);
            futures.forEach(future -> {
                try {
                    var taskResult = future.get();
                    endpointStatus.put(taskResult.externalEndpointId, taskResult.status);
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error while waiting for the health check tasks to finish.", e);
                }
            });
        } catch (InterruptedException e) {
            log.error("Error while waiting for the health check tasks to finish.", e);
        }
        return endpointStatus;
    }

    /**
     * Get the number of endpoints.
     *
     * @return The number of endpoints.
     */
    public long getNrOfEndpoints() {
        return endpointRepository.countByEndpointType(EndpointType.NON_VIRTUAL);
    }

    public long getNrOfVirtualEndpoints() {
        return endpointRepository.countByEndpointType(EndpointType.VIRTUAL);
    }

    /**
     * Internal class as a wrapper for the result of the health check.
     *
     * @param externalEndpointId The external endpoint id.
     * @param status             The status of the endpoint.
     */
    private record TaskResult(String externalEndpointId, Integer status) {
    }

    private void waitUntilAllTasksAreDone(List<Future<TaskResult>> futures) {
        while (futures.stream().anyMatch(future -> !future.isDone())) {
            try {
                //noinspection BusyWait
                Thread.sleep(pollingInterval);
            } catch (InterruptedException e) {
                log.error("Error while waiting for the health check tasks to finish.", e);
            }
        }
    }

    private Callable<TaskResult> createHealthCheckTask(String externalEndpointId) {
        return () -> {
            try {
                var healthStatus = determineHealthStatus(externalEndpointId);
                return switch (healthStatus.getHealthStatus()) {
                    case HEALTHY -> new TaskResult(externalEndpointId, HttpStatus.OK.value());
                    case PENDING -> new TaskResult(externalEndpointId, HttpStatus.PROCESSING.value());
                    case UNHEALTHY -> new TaskResult(externalEndpointId, HttpStatus.SERVICE_UNAVAILABLE.value());
                    case UNKNOWN -> new TaskResult(externalEndpointId, HttpStatus.BAD_REQUEST.value());
                };
            } catch (BusinessException e) {
                return new TaskResult(externalEndpointId, e.getErrorMessage().getHttpStatus().value());
            }
        };
    }
}
