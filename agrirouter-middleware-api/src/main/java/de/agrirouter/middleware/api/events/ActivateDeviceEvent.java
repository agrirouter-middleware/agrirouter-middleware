package de.agrirouter.middleware.api.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * This event will be thrown if the subscriptions for the endpoint should be updated.
 */
@Getter
public class ActivateDeviceEvent extends ApplicationEvent {

    /**
     * The ID of the team set.
     */
    private final String teamSetContextId;

    public ActivateDeviceEvent(Object source, String teamSetContextId) {
        super(source);
        this.teamSetContextId = teamSetContextId;
    }

}
