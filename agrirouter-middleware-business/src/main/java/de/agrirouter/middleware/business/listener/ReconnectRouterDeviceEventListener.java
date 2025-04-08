package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.events.ReconnectRouterDeviceEvent;
import de.agrirouter.middleware.integration.mqtt.MqttConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconnectRouterDeviceEventListener {

    private final MqttConnectionManager mqttConnectionManager;

    @EventListener
    public void reconnectRouterDevice(ReconnectRouterDeviceEvent reconnectRouterDeviceEvent) {
        mqttConnectionManager.tryToReconnect(reconnectRouterDeviceEvent.getIdOfTheRouterDevice());
    }

}
