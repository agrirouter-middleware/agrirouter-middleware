package de.agrirouter.middleware.api.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * This event will be thrown if there are any existing messages that have to be confirmed.
 */
@Getter
public class SaveAndConfirmExistingMessagesEvent extends ApplicationEvent {

    /**
     * The ID of the endpoint to save and confirm the messages for.
     */
    private final String internalEndpointId;

    public SaveAndConfirmExistingMessagesEvent(Object source, String internalEndpointId) {
        super(source);
        this.internalEndpointId = internalEndpointId;
    }

}
