package de.agrirouter.middleware.business;

import de.agrirouter.middleware.business.cache.endpoints.InternalEndpointCache;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Internal service to remove an endpoint incl. the data from the database.
 */
@Slf4j
@Service
public class RemoveEndpointDataService {
    private final MqttClientManagementService mqttClientManagementService;
    private final ErrorRepository errorRepository;
    private final WarningRepository warningRepository;
    private final EndpointRepository endpointRepository;
    private final InternalEndpointCache internalEndpointCache;
    private final UnprocessedMessageRepository unprocessedMessageRepository;
    private final ContentMessageRepository contentMessageRepository;
    private final DeviceDescriptionRepository deviceDescriptionRepository;
    private final TimeLogRepository timeLogRepository;

    public RemoveEndpointDataService(MqttClientManagementService mqttClientManagementService,
                                     ErrorRepository errorRepository,
                                     WarningRepository warningRepository,
                                     EndpointRepository endpointRepository,
                                     InternalEndpointCache internalEndpointCache,
                                     UnprocessedMessageRepository unprocessedMessageRepository,
                                     ContentMessageRepository contentMessageRepository,
                                     DeviceDescriptionRepository deviceDescriptionRepository,
                                     TimeLogRepository timeLogRepository) {
        this.mqttClientManagementService = mqttClientManagementService;
        this.errorRepository = errorRepository;
        this.warningRepository = warningRepository;
        this.endpointRepository = endpointRepository;
        this.internalEndpointCache = internalEndpointCache;
        this.unprocessedMessageRepository = unprocessedMessageRepository;
        this.contentMessageRepository = contentMessageRepository;
        this.deviceDescriptionRepository = deviceDescriptionRepository;
        this.timeLogRepository = timeLogRepository;
    }

    /**
     * Remove the endpoint from the database.
     *
     * @param endpoint THe endpoint to remove.
     */
    @Transactional
    public void removeEndpointData(Endpoint endpoint) {
        log.debug("Disconnect the endpoint.");
        mqttClientManagementService.disconnect(endpoint);

        log.debug("Remove all errors, warnings and information.");
        errorRepository.deleteAllByEndpoint(endpoint);
        warningRepository.deleteAllByEndpoint(endpoint);

        log.debug("Remove endpoint from internal cache to avoid problems.");
        internalEndpointCache.remove(endpoint.getExternalEndpointId());
    }

    /**
     * Remove the endpoint from the database.
     *
     * @param endpoint THe endpoint to remove.
     */
    @Transactional
    public void removeEndpointDataAndEndpoint(Endpoint endpoint) {
        log.debug("Disconnect the endpoint.");
        mqttClientManagementService.disconnect(endpoint);

        log.debug("Remove all errors, warnings and information.");
        errorRepository.deleteAllByEndpoint(endpoint);
        warningRepository.deleteAllByEndpoint(endpoint);

        log.debug("Remove the endpoint.");
        endpointRepository.delete(endpoint);

        log.debug("Remove endpoint from internal cache to avoid problems.");
        internalEndpointCache.remove(endpoint.getExternalEndpointId());
    }

    /**
     * Remove all data for the endpoint.
     *
     * @param sensorAlternateId The sensor alternate ID.
     */
    @Async
    @Transactional
    public void removeData(String sensorAlternateId) {
        log.debug("Remove all unprocessed messages.");
        unprocessedMessageRepository.deleteAllByAgrirouterEndpointId(sensorAlternateId);

        log.debug("Remove the content messages for the endpoint.");
        contentMessageRepository.deleteAllByAgrirouterEndpointId(sensorAlternateId);

        log.debug("Remove device descriptions.");
        deviceDescriptionRepository.deleteAllByAgrirouterEndpointId(sensorAlternateId);

        log.debug("Remove time logs.");
        timeLogRepository.deleteAllByAgrirouterEndpointId(sensorAlternateId);
    }

}
