package de.agrirouter.middleware.api.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * This event is thrown if a dedicated endpoint should be disabled.
 */
@Getter
public class DeactivateEndpointEvent extends ApplicationEvent {

    /**
     * The ID of the endpoint to fetch the endpoint from the database.
     */
    private final String agrirouterEndpointId;

    public DeactivateEndpointEvent(Object source, String agrirouterEndpointId) {
        super(source);
        this.agrirouterEndpointId = agrirouterEndpointId;
    }

}
