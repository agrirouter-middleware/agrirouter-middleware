package de.agrirouter.middleware.integration.mqtt.list_endpoints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Container for the health status messages.
 */
@Slf4j
@Component
public class ListEndpointsMessages {

    private final Map<String, ListEndpointsMessage> listEndpointsMessages = new HashMap<>();

    /**
     * Place the health status message for the given endpoint ID within the container.
     *
     * @param listEndpointsMessage The health status message.
     */
    public void put(ListEndpointsMessage listEndpointsMessage) {
        listEndpointsMessages.put(listEndpointsMessage.getAgrirouterEndpointId(), listEndpointsMessage);
    }

    /**
     * Remove the health status message for the given endpoint ID from the container.
     *
     * @param agrirouterEndpointId The endpoint ID.
     */
    public void remove(String agrirouterEndpointId) {
        var listEndpointsMessage = listEndpointsMessages.remove(agrirouterEndpointId);
        if (listEndpointsMessage == null) {
            log.warn("No list endpoints message found for endpoint ID {}.", agrirouterEndpointId);
        }
    }

    /**
     * Get the list endpoints message for the given endpoint ID.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @return The list endpoints message.
     */
    public ListEndpointsMessage get(String agrirouterEndpointId) {
        var listEndpointsMessage = listEndpointsMessages.get(agrirouterEndpointId);
        if (listEndpointsMessage == null) {
            log.warn("No list endpoints message found for endpoint ID {}.", agrirouterEndpointId);
        }
        return listEndpointsMessage;
    }
}
