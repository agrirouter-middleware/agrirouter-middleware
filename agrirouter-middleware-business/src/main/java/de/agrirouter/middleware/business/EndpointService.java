package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
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
import de.agrirouter.middleware.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Business operations regarding the endpoints.
 */
@Service
public class EndpointService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointService.class);

    private final EndpointRepository endpointRepository;
    private final DecodeMessageService decodeMessageService;
    private final ErrorRepository errorRepository;
    private final WarningRepository warningRepository;
    private final EndpointIntegrationService endpointIntegrationService;
    private final ApplicationRepository applicationRepository;
    private final MqttClientManagementService mqttClientManagementService;
    private final ContentMessageRepository contentMessageRepository;
    private final UnprocessedMessageRepository unprocessedMessageRepository;
    private final RevokeProcessIntegrationService revokeProcessIntegrationService;
    private final DeviceDescriptionRepository deviceDescriptionRepository;
    private final TimeLogRepository timeLogRepository;
    private final BusinessOperationLogService businessOperationLogService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;

    public EndpointService(EndpointRepository endpointRepository,
                           DecodeMessageService decodeMessageService,
                           ErrorRepository errorRepository,
                           WarningRepository warningRepository,
                           EndpointIntegrationService endpointIntegrationService,
                           ApplicationRepository applicationRepository,
                           MqttClientManagementService mqttClientManagementService,
                           ContentMessageRepository contentMessageRepository,
                           UnprocessedMessageRepository unprocessedMessageRepository,
                           RevokeProcessIntegrationService revokeProcessIntegrationService,
                           DeviceDescriptionRepository deviceDescriptionRepository,
                           TimeLogRepository timeLogRepository,
                           BusinessOperationLogService businessOperationLogService,
                           MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService) {
        this.endpointRepository = endpointRepository;
        this.decodeMessageService = decodeMessageService;
        this.errorRepository = errorRepository;
        this.warningRepository = warningRepository;
        this.endpointIntegrationService = endpointIntegrationService;
        this.applicationRepository = applicationRepository;
        this.mqttClientManagementService = mqttClientManagementService;
        this.contentMessageRepository = contentMessageRepository;
        this.unprocessedMessageRepository = unprocessedMessageRepository;
        this.revokeProcessIntegrationService = revokeProcessIntegrationService;
        this.deviceDescriptionRepository = deviceDescriptionRepository;
        this.timeLogRepository = timeLogRepository;
        this.businessOperationLogService = businessOperationLogService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
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
        LOGGER.debug("Update status of the endpoint.");
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
        LOGGER.debug("Update status of the endpoint.");
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
            LOGGER.warn("Endpoint with agrirouter endpoint ID {} not found.", agrirouterEndpointId);
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
        LOGGER.debug("Disconnect the endpoint.");
        mqttClientManagementService.disconnect(endpoint.asOnboardingResponse());
        LOGGER.debug("Remove the data for each connected virtual CU  incl. status, errors, warnings and so on.");
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

        LOGGER.debug("Remove all unprocessed messages.");
        unprocessedMessageRepository.deleteAllByAgrirouterEndpointId(sensorAlternateId);

        LOGGER.debug("Remove all errors, warnings and information.");
        errorRepository.deleteAllByEndpoint(endpoint);
        warningRepository.deleteAllByEndpoint(endpoint);

        LOGGER.debug("Remove the content messages for the endpoint.");
        contentMessageRepository.deleteAllByAgrirouterEndpointId(sensorAlternateId);

        LOGGER.debug("Remove device descriptions.");
        deviceDescriptionRepository.deleteAllByAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());

        LOGGER.debug("Remove time logs.");
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
            LOGGER.debug("Deactivate the endpoint to avoid race conditions.");
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
            LOGGER.warn("Could not find endpoint with agrirouter ID {}.", agrirouterEndpointId);
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

}
