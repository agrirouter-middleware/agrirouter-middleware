package de.agrirouter.middleware.api.events;

import org.springframework.context.ApplicationEvent;

/**
 * Will be thrown if the connections need to be checked after connection loss for example.
 */
public class CheckConnectionsEvent extends ApplicationEvent {

    public CheckConnectionsEvent(Object source) {
        super(source);
    }

}
