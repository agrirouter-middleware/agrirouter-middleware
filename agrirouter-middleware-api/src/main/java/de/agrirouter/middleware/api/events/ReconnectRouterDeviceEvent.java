package de.agrirouter.middleware.api.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * This event is thrown if the router device should be reconnected.
 */
@Getter
public class ReconnectRouterDeviceEvent extends ApplicationEvent {

    private final String idOfTheRouterDevice;

    public ReconnectRouterDeviceEvent(Object source, String idOfTheRouterDevice) {
        super(source);
        this.idOfTheRouterDevice = idOfTheRouterDevice;
    }
}
