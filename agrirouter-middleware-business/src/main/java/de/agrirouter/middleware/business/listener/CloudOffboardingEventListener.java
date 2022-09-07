package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.business.events.CloudOffboardingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to handle the message acknowledgement events in case of a cloud offboarding.
 */
@Service
public class CloudOffboardingEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudOffboardingEventListener.class);

    private final EndpointService endpointService;

    public CloudOffboardingEventListener(EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    /**
     * On cloud offboarding event, all virtual endpoints will be deleted.
     *
     * @param cloudOffboardingEvent The event.
     */
    @EventListener
    @Transactional
    public void offboardCloudEndpoint(CloudOffboardingEvent cloudOffboardingEvent) {
        LOGGER.debug("Incoming event for cloud offboarding.");
        cloudOffboardingEvent.getVirtualEndpointIds().forEach(endpointService::deactivateEndpointByAgrirouterId);
        cloudOffboardingEvent.getVirtualEndpointIds().forEach(endpointService::deleteEndpointDataFromTheMiddlewareByAgrirouterId);
    }
}
