package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.events.ClearSubscriptionsForMqttClientEvent;
import de.agrirouter.middleware.integration.mqtt.MqttConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClearSubscriptionsForMqttClientEventListener {

    private final MqttConnectionManager mqttConnectionManager;

    @EventListener
    public void clearSubscriptionsForRouterDevice(ClearSubscriptionsForMqttClientEvent clearSubscriptionsForMqttClientEvent) {
        mqttConnectionManager.clearSubscriptionsForMqttClient(clearSubscriptionsForMqttClientEvent.getIdOfTheRouterDevice());
    }

}
