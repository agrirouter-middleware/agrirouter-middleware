package de.agrirouter.middleware.api.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that will be thrown if there is a connection loss.
 */
@Getter
public class RemoveStaleConnectionsEvent extends ApplicationEvent {

    public RemoveStaleConnectionsEvent(Object source) {
        super(source);
    }
}
