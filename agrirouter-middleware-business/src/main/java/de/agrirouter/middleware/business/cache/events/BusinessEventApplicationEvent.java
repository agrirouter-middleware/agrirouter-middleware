package de.agrirouter.middleware.business.cache.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Application event for business events.
 */
@Getter
public class BusinessEventApplicationEvent extends ApplicationEvent {

    /**
     * The external endpoint id.
     */
    private final String externalEndpointId;

    /**
     * The business event.
     */
    private final BusinessEvent businessEvent;

    public BusinessEventApplicationEvent(Object source, String externalEndpointId, BusinessEvent businessEvent) {
        super(source);
        this.externalEndpointId = externalEndpointId;
        this.businessEvent = businessEvent;
    }
}
