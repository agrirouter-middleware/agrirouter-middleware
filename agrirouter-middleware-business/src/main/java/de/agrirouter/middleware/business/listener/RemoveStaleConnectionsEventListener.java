package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.events.RemoveStaleConnectionsEvent;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * This class is used to remove stale connections from the MQTT management service.
 */
@Service
public class RemoveStaleConnectionsEventListener {

    private final MqttClientManagementService mqttClientManagementService;

    /**
     * Constructor.
     *
     * @param mqttClientManagementService MqttClientManagementService
     */
    public RemoveStaleConnectionsEventListener(MqttClientManagementService mqttClientManagementService) {
        this.mqttClientManagementService = mqttClientManagementService;
    }

    /**
     * Removes stale connections from the MQTT management service.
     */
    @EventListener(classes = RemoveStaleConnectionsEvent.class)
    public void removeStaleConnections() {
        mqttClientManagementService.removeStaleConnections();
    }
}
