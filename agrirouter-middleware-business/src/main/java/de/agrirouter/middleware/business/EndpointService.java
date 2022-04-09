package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.businesslog.BusinessLogService;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.enums.EndpointType;
import de.agrirouter.middleware.domain.log.Error;
import de.agrirouter.middleware.domain.log.Warning;
import de.agrirouter.middleware.integration.EndpointIntegrationService;
import de.agrirouter.middleware.integration.RevokeProcessIntegrationService;
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
    private final BusinessLogService businessLogService;
    private final ErrorRepository errorRepository;
    private final WarningRepository warningRepository;
    private final EndpointIntegrationService endpointIntegrationService;
    private final ApplicationRepository applicationRepository;
    private final MqttClientManagementService mqttClientManagementService;
    private final ContentMessageRepository contentMessageRepository;
    private final UnprocessedMessageRepository unprocessedMessageRepository;
    private final BusinessLogEventRepository businessLogEventRepository;
    private final RevokeProcessIntegrationService revokeProcessIntegrationService;
    private final DeviceDescriptionRepository deviceDescriptionRepository;
    private final TimeLogRepository timeLogRepository;

    public EndpointService(EndpointRepository endpointRepository,
                           DecodeMessageService decodeMessageService,
                           BusinessLogService businessLogService,
                           ErrorRepository errorRepository,
                           WarningRepository warningRepository,
                           EndpointIntegrationService endpointIntegrationService,
                           ApplicationRepository applicationRepository,
                           MqttClientManagementService mqttClientManagementService,
                           ContentMessageRepository contentMessageRepository,
                           UnprocessedMessageRepository unprocessedMessageRepository,
                           BusinessLogEventRepository businessLogEventRepository,
                           RevokeProcessIntegrationService revokeProcessIntegrationService,
                           DeviceDescriptionRepository deviceDescriptionRepository, TimeLogRepository timeLogRepository) {
        this.endpointRepository = endpointRepository;
        this.decodeMessageService = decodeMessageService;
        this.businessLogService = businessLogService;
        this.errorRepository = errorRepository;
        this.warningRepository = warningRepository;
        this.endpointIntegrationService = endpointIntegrationService;
        this.applicationRepository = applicationRepository;
        this.mqttClientManagementService = mqttClientManagementService;
        this.contentMessageRepository = contentMessageRepository;
        this.unprocessedMessageRepository = unprocessedMessageRepository;
        this.businessLogEventRepository = businessLogEventRepository;
        this.revokeProcessIntegrationService = revokeProcessIntegrationService;
        this.deviceDescriptionRepository = deviceDescriptionRepository;
        this.timeLogRepository = timeLogRepository;
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
    }

    /**
     * Disable an endpoint manually and do not increase the threshold.
     *
     * @param externalEndpointId -
     */
    @Async
    public void deactivateEndpointButDoNotIncreaseThreshold(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDisabled(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            LOGGER.warn("The endpoint with the id '{}' was deactivated.", endpoint.getExternalEndpointId());
            LOGGER.warn("The endpoint with the AR id '{}' was deactivated.", endpoint.getAgrirouterEndpointId());
            endpoint.setDeactivated(true);
            endpointRepository.save(endpoint);
            businessLogService.endpointDeactivated(endpoint);
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }

    /**
     * Delete the endpoint and remove all the data.
     *
     * @param agrirouterEndpointId -
     */
    public void deleteEndpointDataFromTheMiddlewareByAgrirouterId(String agrirouterEndpointId) {
        final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(agrirouterEndpointId);
        if (optionalEndpoint.isPresent()) {
            deleteEndpointData(optionalEndpoint.get().getExternalEndpointId());
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }

    /**
     * Delete the endpoint and remove all the data.
     *
     * @param externalEndpointId -
     */
    public void deleteEndpointData(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            LOGGER.debug("Disconnect the endpoint.");
            mqttClientManagementService.disconnect(optionalEndpoint.get().asOnboardingResponse());
            LOGGER.debug("Remove the data for each connected virtual CU  incl. status, errors, warnings and so on.");
            endpoint.getConnectedVirtualEndpoints().forEach(this::deleteEndpointData);
            deleteEndpointData(endpoint);
            endpointRepository.delete(endpoint);
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }

    private void deleteEndpointData(Endpoint endpoint) {
        final var sensorAlternateId = endpoint.asOnboardingResponse().getSensorAlternateId();

        LOGGER.debug("Remove all unprocessed messages.");
        unprocessedMessageRepository.deleteAllByAgrirouterEndpointId(sensorAlternateId);

        LOGGER.debug("Remove the content messages for the endpoint.");
        contentMessageRepository.deleteAllByAgrirouterEndpointId(sensorAlternateId);

        LOGGER.debug("Remove all business log events.");
        businessLogEventRepository.deleteAllByEndpoint(endpoint);

        LOGGER.debug("Remove all errors, warnings and information.");
        errorRepository.deleteAllByEndpoint(endpoint);
        warningRepository.deleteAllByEndpoint(endpoint);

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
    public void resendCapabilities(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDisabled(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var optionalApplication = applicationRepository.findByEndpointsContains(optionalEndpoint.get());
            if (optionalApplication.isPresent()) {
                sendCapabilities(optionalApplication.get(), optionalEndpoint.get());
                businessLogService.resendCapabilities(optionalEndpoint.get());
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
        businessLogService.sendCapabilities(endpoint);
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
                    deleteEndpointData(externalEndpointId);
                } else {
                    throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
                }
            }
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }
}
