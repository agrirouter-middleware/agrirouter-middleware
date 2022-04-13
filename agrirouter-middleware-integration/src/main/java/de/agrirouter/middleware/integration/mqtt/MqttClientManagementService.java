package de.agrirouter.middleware.integration.mqtt;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.Gateway;
import com.dke.data.agrirouter.convenience.mqtt.client.MqttClientService;
import com.dke.data.agrirouter.convenience.mqtt.client.MqttOptionService;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.time.Instant;
import java.util.*;

/**
 * Centralized management for all the applications.
 */
@Service
@ApplicationScope
public class MqttClientManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttClientManagementService.class);

    private final Map<String, CachedMqttClient> cachedMqttClients;
    private final MqttClientService mqttClientService;
    private final MqttOptionService mqttOptionService;
    private final MessageHandlingCallback messageHandlingCallback;

    public MqttClientManagementService(MqttClientService mqttClientService,
                                       MqttOptionService mqttOptionService,
                                       MessageHandlingCallback messageHandlingCallback) {
        this.mqttClientService = mqttClientService;
        this.mqttOptionService = mqttOptionService;
        this.messageHandlingCallback = messageHandlingCallback;
        this.cachedMqttClients = new HashMap<>();
    }

    /**
     * Get or create an MQTT client for the given onboard response.
     * The client will be connected to the AR and subscribes to the available topics.
     *
     * @param onboardingResponse -
     * @return -
     */
    public Optional<IMqttClient> get(OnboardingResponse onboardingResponse) {
        if (Gateway.MQTT.getKey().equals(onboardingResponse.getConnectionCriteria().getGatewayId())) {
            final CachedMqttClient cachedMqttClient = getCachedMqttClient(onboardingResponse);
            if (!isConnected(cachedMqttClient)) {
                try {
                    LOGGER.debug("The existing mqtt client connection for endpoint with the MQTT client ID '{}' is no longer connected, therefore removing this one from the cache and reconnecting the endpoint.", onboardingResponse.getConnectionCriteria().getClientId());
                    cachedMqttClients.remove(cachedMqttClient.getId());
                    final var mqttClient = initMqttClient(onboardingResponse);
                    final var newCachedMqttClient = new CachedMqttClient(onboardingResponse.getSensorAlternateId(), onboardingResponse.getConnectionCriteria().getClientId(), Optional.of(mqttClient), cachedMqttClient.getConnectionErrors());
                    cachedMqttClients.put(onboardingResponse.getConnectionCriteria().getClientId(), newCachedMqttClient);
                    return Optional.of(mqttClient);
                } catch (Exception e) {
                    cachedMqttClient.getConnectionErrors().add(new ConnectionError(Instant.now(), String.format("There was an error while connecting the client, the error message was '%s'.", e.getMessage())));
                    cachedMqttClients.put(onboardingResponse.getConnectionCriteria().getClientId(), cachedMqttClient);
                }
            } else {
                LOGGER.debug("Returning existing mqtt client for endpoint with the MQTT client ID '{}'.", onboardingResponse.getConnectionCriteria().getClientId());
                //noinspection OptionalGetWithoutIsPresent
                return Optional.of(cachedMqttClient.getMqttClient().get());
            }
        } else {
            LOGGER.debug("This onboard response is not MQTT ready, the gateway is set to {}.", onboardingResponse.getConnectionCriteria().getGatewayId());
        }
        return Optional.empty();
    }

    private boolean isConnected(CachedMqttClient cachedMqttClient) {
        return cachedMqttClient.getMqttClient().isPresent() && cachedMqttClient.getMqttClient().get().isConnected();
    }

    private CachedMqttClient getCachedMqttClient(OnboardingResponse onboardingResponse) {
        final var cachedMqttClient = cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
        if (null == cachedMqttClient) {
            LOGGER.debug("Did not find a mqtt client for endpoint with the MQTT client ID '{}'. Creating a new one.", onboardingResponse.getConnectionCriteria().getClientId());
            final var newCachedMqttClient = new CachedMqttClient(onboardingResponse.getSensorAlternateId(), onboardingResponse.getConnectionCriteria().getClientId(), Optional.empty(), new ArrayList<>());
            cachedMqttClients.put(onboardingResponse.getConnectionCriteria().getClientId(), newCachedMqttClient);
        }
        return cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
    }

    private IMqttClient initMqttClient(OnboardingResponse onboardingResponse) throws MqttException {
        IMqttClient mqttClient = mqttClientService.create(onboardingResponse);
        final var mqttConnectOptions = mqttOptionService.createMqttConnectOptions(onboardingResponse);
        mqttConnectOptions.setConnectionTimeout(60);
        mqttConnectOptions.setKeepAliveInterval(60 * 60);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttClient.connect(mqttConnectOptions);
        mqttClient.subscribe(onboardingResponse.getConnectionCriteria().getCommands());
        mqttClient.setCallback(messageHandlingCallback);
        return mqttClient;
    }

    /**
     * Get the state of a MQTT connection.
     *
     * @param onboardingResponse -
     * @return -
     */
    public ConnectionState getState(OnboardingResponse onboardingResponse) {
        final var cachedMqttClient = cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
        return new ConnectionState(cachedMqttClient != null ? cachedMqttClient.getId() : null, cachedMqttClient != null,
                cachedMqttClient != null && cachedMqttClient.getMqttClient().isPresent() && cachedMqttClient.getMqttClient().get().isConnected(),
                cachedMqttClient != null ? cachedMqttClient.getConnectionErrors() : Collections.emptyList());
    }

    /**
     * Disconnect an existing connection.
     *
     * @param onboardingResponse -
     */
    public void disconnect(OnboardingResponse onboardingResponse) {
        final var cachedMqttClient = cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
        if (null != cachedMqttClient) {
            cachedMqttClient.getMqttClient().ifPresent(iMqttClient -> {
                try {
                    cachedMqttClients.remove(onboardingResponse.getConnectionCriteria().getClientId());
                    iMqttClient.disconnect();
                } catch (MqttException e) {
                    LOGGER.error("Could not disconnect the MQTT client.", e);
                }
            });
        }
    }

    /**
     * Remove stale connections in case there was a connection loss.
     */
    public void removeStaleConnections() {
        cachedMqttClients.values().removeIf(cachedMqttClient -> cachedMqttClient.getMqttClient().isEmpty() || !cachedMqttClient.getMqttClient().get().isConnected());
    }
}
