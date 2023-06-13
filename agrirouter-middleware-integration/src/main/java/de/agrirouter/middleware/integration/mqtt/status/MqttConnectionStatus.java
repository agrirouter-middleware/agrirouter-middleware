package de.agrirouter.middleware.integration.mqtt.status;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Container holding the application connection status.
 */
@Getter
@Setter
@Builder
public class MqttConnectionStatus {

    private final String key;
    private final String clientId;
    private final String connectionStatus;

}
