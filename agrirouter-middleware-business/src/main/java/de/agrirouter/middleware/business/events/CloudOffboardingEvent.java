package de.agrirouter.middleware.business.events;

import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Event to be published if a cloud endpoint is offboarded.
 */
public class CloudOffboardingEvent extends ApplicationEvent {

    private final List<String> virtualEndpointIds;

    public CloudOffboardingEvent(Object source, List<String> virtualEndpointIds) {
        super(source);
        this.virtualEndpointIds = virtualEndpointIds;
    }

    public List<String> getVirtualEndpointIds() {
        return virtualEndpointIds;
    }
}
