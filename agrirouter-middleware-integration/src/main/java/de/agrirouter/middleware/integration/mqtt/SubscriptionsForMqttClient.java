package de.agrirouter.middleware.integration.mqtt;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The subscriptions for a MQTT client.
¶ */
@Getter
@Component
public class SubscriptionsForMqttClient {
    private final Map<String, Set<String>> subscriptions;

    public SubscriptionsForMqttClient() {
        subscriptions = new HashMap<>();
    }

    /**
     * Add a subscription.
     */
    public void add(String clientId, String topic) {
        var subscriptionsForClientId = subscriptions.get(clientId);
        if (subscriptionsForClientId == null) {
            subscriptionsForClientId = new HashSet<>();
        }
        subscriptionsForClientId.add(topic);
        subscriptions.put(clientId, subscriptionsForClientId);
    }

    /**
     * Check is a subscription for a client ID and the sensor alternate ID exists.
     */
    public boolean exists(String clientId, String topic) {
        var subscriptionsForClientId = subscriptions.get(clientId);
        if (subscriptionsForClientId == null) {
            return false;
        }
        return subscriptionsForClientId.contains(topic);
    }
}
