package de.agrirouter.middleware.integration.mqtt;

import com.dke.data.agrirouter.api.enums.Gateway;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.mqtt.status.MqttConnectionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Centralized management for all the applications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MqttClientManagementService {

    private final MqttConnectionManager mqttConnectionManager;

    /**
     * Get or create an MQTT client for the given onboard response.
     * The client will be connected to the AR and subscribes to the available topics.
     *
     * @param endpoint The endpoint for which the MQTT client should be created.
     * @return The MQTT client.
     */
    public Optional<IMqttClient> get(Endpoint endpoint) {
        var onboardingResponse = endpoint.asOnboardingResponse();
        if (Gateway.MQTT.getKey().equals(onboardingResponse.getConnectionCriteria().getGatewayId())) {
            return mqttConnectionManager.getCachedMqttClient(onboardingResponse).mqttClient();
        }
        return Optional.empty();
    }

    /**
     * Determine the technical connection state.
     *
     * @param endpoint -
     * @return -
     */
    public TechnicalConnectionState getTechnicalState(Endpoint endpoint) {
        return mqttConnectionManager.getTechnicalState(endpoint);
    }

    /**
     * Get all pending delivery tokens for the endpoint.
     *
     * @param endpoint The endpoint.
     * @return The list of pending delivery tokens.
     */
    public List<IMqttDeliveryToken> getPendingDeliveryTokens(Endpoint endpoint) {
        return mqttConnectionManager.getPendingDeliveryTokens(endpoint);
    }

    /**
     * Clear the connection errors.
     *
     * @param endpoint The endpoint.
     */
    public void clearConnectionErrors(Endpoint endpoint) {
        mqttConnectionManager.clearConnectionErrors(endpoint);
    }

    /**
     * Count the number of active connections.
     *
     * @return The number of active connections.
     */
    public long getNumberOfActiveConnections() {
        return mqttConnectionManager.getNumberOfActiveConnections();
    }

    /**
     * Count the number of inactive connections.
     *
     * @return The number of inactive connections.
     */
    public long getNumberOfInactiveConnections() {
        return mqttConnectionManager.getNumberOfInactiveConnections();
    }

    /**
     * Get the connection status for all MQTT clients.
     *
     * @return The connection status for all MQTT clients.
     */
    public List<MqttConnectionStatus> getMqttConnectionStatus() {
        return mqttConnectionManager.getMqttConnectionStatus();
    }

    /**
     * Get the state of a MQTT connection.
     *
     * @param endpoint The endpoint.
     * @return The connection state.
     */
    public ConnectionState getState(Endpoint endpoint) {
        return mqttConnectionManager.getState(endpoint);
    }
}
