package de.agrirouter.middleware.api.events;

import org.springframework.context.ApplicationEvent;

/**
 * This event will be thrown if all endpoints are reconnected.
 */
public class AllEndpointsReconnectedEvent extends ApplicationEvent {

    /**
     * Constructor.
     *
     * @param source The source of the event.
     */
    public AllEndpointsReconnectedEvent(Object source) {
        super(source);
    }
}
