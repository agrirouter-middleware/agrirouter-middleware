package de.agrirouter.middleware.business;

import de.agrirouter.middleware.business.cache.endpoints.InternalEndpointCache;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.persistence.jpa.ContentMessageRepository;
import de.agrirouter.middleware.persistence.jpa.EndpointRepository;
import de.agrirouter.middleware.persistence.jpa.UnprocessedMessageRepository;
import de.agrirouter.middleware.persistence.mongo.DeviceDescriptionRepository;
import de.agrirouter.middleware.persistence.mongo.TimeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Internal service to remove an endpoint incl. the data from the database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RemoveEndpointDataService {

    private final EndpointRepository endpointRepository;
    private final InternalEndpointCache internalEndpointCache;
    private final UnprocessedMessageRepository unprocessedMessageRepository;
    private final ContentMessageRepository contentMessageRepository;
    private final DeviceDescriptionRepository deviceDescriptionRepository;
    private final TimeLogRepository timeLogRepository;

    /**
     * Remove the endpoint from the database.
     *
     * @param endpoint THe endpoint to remove.
     */
    @Transactional
    public void removeEndpointData(Endpoint endpoint) {
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
