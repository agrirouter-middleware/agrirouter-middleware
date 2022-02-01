package de.agrirouter.middleware.api.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * This event is thrown if the capabilities for the application are updated.
 */
@Getter
public class ResendCapabilitiesForApplicationEvent extends ApplicationEvent {

    /**
     * The application that was updated.
     */
    private final String internalApplicationId;

    public ResendCapabilitiesForApplicationEvent(Object source, String internalApplicationId) {
        super(source);
        this.internalApplicationId = internalApplicationId;
    }

}
