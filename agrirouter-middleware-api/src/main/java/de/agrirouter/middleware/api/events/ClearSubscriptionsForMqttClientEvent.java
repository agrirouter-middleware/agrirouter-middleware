package de.agrirouter.middleware.api.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * This event is thrown if the subscriptions for the MQTT client should be cleared.
 */
@Getter
public class ClearSubscriptionsForMqttClientEvent extends ApplicationEvent {

    private final String idOfTheRouterDevice;

    public ClearSubscriptionsForMqttClientEvent(Object source, String idOfTheRouterDevice) {
        super(source);
        this.idOfTheRouterDevice = idOfTheRouterDevice;

    }
}
