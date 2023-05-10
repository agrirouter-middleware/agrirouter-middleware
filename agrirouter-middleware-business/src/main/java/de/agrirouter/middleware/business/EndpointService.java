package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
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
import de.agrirouter.middleware.integration.mqtt.health.HealthStatusIntegrationService;
import de.agrirouter.middleware.integration.mqtt.list_endpoints.ListEndpointsIntegrationService;
import de.agrirouter.middleware.integration.mqtt.list_endpoints.MessageRecipient;
import de.agrirouter.middleware.integration.mqtt.list_endpoints.cache.MessageRecipientCache;
import de.agrirouter.middleware.integration.status.AgrirouterStatusIntegrationService;
import de.agrirouter.middleware.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Business operations regarding the endpoints.
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
    private final ContentMessageRepository contentMessageRepository;
    private final UnprocessedMessageRepository unprocessedMessageRepository;
    private final RevokeProcessIntegrationService revokeProcessIntegrationService;
    private final DeviceDescriptionRepository deviceDescriptionRepository;
    private final TimeLogRepository timeLogRepository;
    private final BusinessOperationLogService businessOperationLogService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final BusinessEventsCache businessEventsCache;
    private final ListEndpointsIntegrationService listEndpointsIntegrationService;
    private final MessageRecipientCache messageRecipientCache;
    private final AgrirouterStatusIntegrationService agrirouterStatusIntegrationService;

    @Value("${app.agrirouter.mqtt.synchronous.response.wait.time}")
    private int nrOfMillisecondsToWaitForTheResponseOfTheAgrirouter;

    @Value("${app.agrirouter.mqtt.synchronous.response.polling.intervall}")
    private int pollingIntervall;

    public EndpointService(EndpointRepository endpointRepository,
                           DecodeMessageService decodeMessageService,
                           ErrorRepository errorRepository,
                           WarningRepository warningRepository,
                           EndpointIntegrationService endpointIntegrationService,
                           ApplicationRepository applicationRepository,
                           MqttClientManagementService mqttClientManagementService,
                           HealthStatusIntegrationService healthStatusIntegrationService,
                           ContentMessageRepository contentMessageRepository,
                           UnprocessedMessageRepository unprocessedMessageRepository,
                           RevokeProcessIntegrationService revokeProcessIntegrationService,
                           DeviceDescriptionRepository deviceDescriptionRepository,
                           TimeLogRepository timeLogRepository,
                           BusinessOperationLogService businessOperationLogService,
                           MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                           BusinessEventsCache businessEventsCache,
                           ListEndpointsIntegrationService listEndpointsIntegrationService,
                           MessageRecipientCache messageRecipientCache,
                           AgrirouterStatusIntegrationService agrirouterStatusIntegrationService) {
        this.endpointRepository = endpointRepository;
        this.decodeMessageService = decodeMessageService;
        this.errorRepository = errorRepository;
        this.warningRepository = warningRepository;
        this.endpointIntegrationService = endpointIntegrationService;
        this.applicationRepository = applicationRepository;
        this.mqttClientManagementService = mqttClientManagementService;
        this.healthStatusIntegrationService = healthStatusIntegrationService;
        this.contentMessageRepository = contentMessageRepository;
        this.unprocessedMessageRepository = unprocessedMessageRepository;
        this.revokeProcessIntegrationService = revokeProcessIntegrationService;
        this.deviceDescriptionRepository = deviceDescriptionRepository;
        this.timeLogRepository = timeLogRepository;
        this.businessOperationLogService = businessOperationLogService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.businessEventsCache = businessEventsCache;
        this.listEndpointsIntegrationService = listEndpointsIntegrationService;
        this.messageRecipientCache = messageRecipientCache;
        this.agrirouterStatusIntegrationService = agrirouterStatusIntegrationService;
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
        final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointIdAndIgnoreDeactivated(agrirouterEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            deleteEndpointData(optionalEndpoint.get().getExternalEndpointId());
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
    public void deleteEndpointData(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            deleteEndpointWithAllDataFromTheMiddleware(endpoint);
        }
    }

    private void deleteEndpointWithAllDataFromTheMiddleware(Endpoint endpoint) {
        log.debug("Disconnect the endpoint.");
        mqttClientManagementService.disconnect(endpoint.asOnboardingResponse());
        log.debug("Remove the data for each connected virtual CU  incl. status, errors, warnings and so on.");
        endpoint.getConnectedVirtualEndpoints().forEach(this::deleteEndpointData);
        deleteEndpointData(endpoint);
        businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Endpoint data has been deleted.");
        endpointRepository.delete(endpoint);
        businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Endpoint was deleted.");
    }

    /**
     * Delete the endpoint and remove all the data.
     *
     * @param externalEndpointId The external endpoint ID.
     */
    @Async
    @Transactional
    public void deleteAllEndpoints(String externalEndpointId) {
        endpointRepository.findAllByExternalEndpointId(externalEndpointId).forEach(this::deleteEndpointWithAllDataFromTheMiddleware);
    }

    private void deleteEndpointData(Endpoint endpoint) {
        final var sensorAlternateId = endpoint.asOnboardingResponse().getSensorAlternateId();

        log.debug("Remove all unprocessed messages.");
        unprocessedMessageRepository.deleteAllByAgrirouterEndpointId(sensorAlternateId);

        log.debug("Remove all errors, warnings and information.");
        errorRepository.deleteAllByEndpoint(endpoint);
        warningRepository.deleteAllByEndpoint(endpoint);

        log.debug("Remove the content messages for the endpoint.");
        contentMessageRepository.deleteAllByAgrirouterEndpointId(sensorAlternateId);

        log.debug("Remove device descriptions.");
        deviceDescriptionRepository.deleteAllByAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());

        log.debug("Remove time logs.");
        timeLogRepository.deleteAllByAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
    }

    /**
     * Resending the capabilities.
     *
     * @param externalEndpointId The internal ID of the endpoint.
     */
    @Async
    @Transactional
    public void resendCapabilities(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDeactivated(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            final var optionalApplication = applicationRepository.findByEndpointsContains(optionalEndpoint.get());
            if (optionalApplication.isPresent()) {
                sendCapabilities(optionalApplication.get(), endpoint);
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Capabilities were resent.");
            } else {
                throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
            }
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
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
        return mqttClientManagementService.getState(endpoint.asOnboardingResponse());
    }

    /**
     * Find the endpoints by their external ID.
     *
     * @param externalEndpointIds -
     * @return -
     */
    public List<Endpoint> findByExternalEndpointIds(List<String> externalEndpointIds) {
        return endpointRepository.findByExternalEndpointIdIsIn(externalEndpointIds);
    }

    /**
     * Find the endpoint by its external ID.
     *
     * @param externalEndpointId -
     * @return -
     */
    public Optional<Endpoint> findByExternalEndpointId(String externalEndpointId) {
        return endpointRepository.findByExternalEndpointId(externalEndpointId);
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
            endpoint.getConnectedVirtualEndpoints().forEach(vcu -> {
                vcu.setDeactivated(true);
                endpointRepository.save(vcu);
            });
            if (EndpointType.NON_VIRTUAL.equals(endpoint.getEndpointType())) {
                final var optionalApplication = applicationRepository.findByEndpointsContains(endpoint);
                if (optionalApplication.isPresent()) {
                    final var application = optionalApplication.get();
                    revokeProcessIntegrationService.revoke(application, endpoint);
                    businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Endpoint was revoked.");
                    deleteEndpointData(externalEndpointId);
                } else {
                    throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
                }
            } else {
                log.warn("Tried to revoke a virtual endpoint with the ID '{}'. This is not possible.", externalEndpointId);
            }
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
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
     * Delete an endpoint.
     *
     * @param endpoint The endpoint.
     */
    public void delete(Endpoint endpoint) {
        endpointRepository.deleteByExternalEndpointId(endpoint.getExternalEndpointId());
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
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
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
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }

    /**
     * Deactivate an endpoint.
     *
     * @param agrirouterEndpointId The ID of the endpoint.
     */
    @Transactional
    public void deactivateEndpointByAgrirouterId(String agrirouterEndpointId) {
        final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(agrirouterEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            endpoint.setDeactivated(true);
            endpointRepository.save(endpoint);
        } else {
            log.warn("Could not find endpoint with agrirouter ID {}.", agrirouterEndpointId);
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
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
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
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
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
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }

    /**
     * Check whether the endpoint is healthy or not. The method will publish a message on
     *
     * @param externalEndpointId The external ID of the endpoint.
     * @return True if the endpoint is healthy, false otherwise.
     */
    public boolean isHealthy(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            healthStatusIntegrationService.publishHealthStatusMessage(endpoint.asOnboardingResponse());
            if (healthStatusIntegrationService.hasPendingResponse(endpoint.getAgrirouterEndpointId())) {
                var timer = nrOfMillisecondsToWaitForTheResponseOfTheAgrirouter;
                while (timer > 0) {
                    try {
                        Thread.sleep(pollingIntervall);
                        if (healthStatusIntegrationService.isHealthy(endpoint.getAgrirouterEndpointId())) {
                            return true;
                        }
                    } catch (InterruptedException e) {
                        log.error("Error while waiting for health status response.", e);
                    }
                    timer = timer - pollingIntervall;
                }

            } else {
                log.warn("There is no pending health status response for endpoint {}.", endpoint.getAgrirouterEndpointId());
            }
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
        return false;
    }

    /**
     * Get the recipients for the endpoint.
     *
     * @return The recipients for the endpoint.
     */
    public Collection<MessageRecipient> getMessageRecipients(String externalEndpointId) {
        if (agrirouterStatusIntegrationService.isOperational()) {
            final var optionalEndpoint = findByExternalEndpointId(externalEndpointId);
            if (optionalEndpoint.isPresent()) {
                final var endpoint = optionalEndpoint.get();
                listEndpointsIntegrationService.publishListEndpointsMessage(endpoint.asOnboardingResponse());
                if (listEndpointsIntegrationService.hasPendingResponse(endpoint.getAgrirouterEndpointId())) {
                    var timer = nrOfMillisecondsToWaitForTheResponseOfTheAgrirouter;
                    while (timer > 0) {
                        try {
                            Thread.sleep(pollingIntervall);
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
                        timer = timer - pollingIntervall;
                    }
                } else {
                    log.warn("There is no pending list endpoints response for endpoint {}.", endpoint.getAgrirouterEndpointId());
                }
            } else {
                throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
            }
            log.debug("Could not find recipients for endpoint '{}', now checking the cache.", externalEndpointId);
            var optionalMessageRecipients = messageRecipientCache.get(externalEndpointId);
            return optionalMessageRecipients.orElse(Collections.emptyList());
        } else {
            log.debug("Agrirouter is not operational, using the cached recipients.");
            var optionalMessageRecipients = messageRecipientCache.get(externalEndpointId);
            return optionalMessageRecipients.orElse(Collections.emptyList());
        }
    }
}
