package de.agrirouter.middleware.integration.mqtt;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.Gateway;
import com.dke.data.agrirouter.convenience.mqtt.client.MqttClientService;
import com.dke.data.agrirouter.convenience.mqtt.client.MqttOptionService;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                    cachedMqttClients.remove(cachedMqttClient.id());
                    final var mqttClient = initMqttClient(onboardingResponse);
                    final var newCachedMqttClient = new CachedMqttClient(onboardingResponse.getSensorAlternateId(), onboardingResponse.getConnectionCriteria().getClientId(), Optional.of(mqttClient), cachedMqttClient.connectionErrors());
                    cachedMqttClients.put(onboardingResponse.getConnectionCriteria().getClientId(), newCachedMqttClient);
                    return Optional.of(mqttClient);
                } catch (Exception e) {
                    cachedMqttClient.connectionErrors().add(new ConnectionError(Instant.now(), String.format("There was an error while connecting the client, the error message was '%s'.", e.getMessage())));
                    cachedMqttClients.put(onboardingResponse.getConnectionCriteria().getClientId(), cachedMqttClient);
                }
            } else {
                LOGGER.debug("Returning existing mqtt client for endpoint with the MQTT client ID '{}'.", onboardingResponse.getConnectionCriteria().getClientId());
                //noinspection OptionalGetWithoutIsPresent
                return Optional.of(cachedMqttClient.mqttClient().get());
            }
        } else {
            LOGGER.debug("This onboard response is not MQTT ready, the gateway is set to {}.", onboardingResponse.getConnectionCriteria().getGatewayId());
        }
        return Optional.empty();
    }

    private boolean isConnected(CachedMqttClient cachedMqttClient) {
        return cachedMqttClient.mqttClient().isPresent() && cachedMqttClient.mqttClient().get().isConnected();
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
        return new ConnectionState(cachedMqttClient != null ? cachedMqttClient.id() : null, cachedMqttClient != null,
                cachedMqttClient != null && cachedMqttClient.mqttClient().isPresent() && cachedMqttClient.mqttClient().get().isConnected(),
                cachedMqttClient != null ? cachedMqttClient.connectionErrors() : Collections.emptyList());
    }

    /**
     * Determine the technical connection state.
     *
     * @param onboardingResponse -
     * @return -
     */
    public TechnicalConnectionState getTechnicalState(Application application, OnboardingResponse onboardingResponse) {
        LOGGER.debug("Fetching the technical state of the MQTT client for endpoint with the MQTT client ID '{}'.", onboardingResponse.getConnectionCriteria().getClientId());
        final var cachedMqttClient = cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
        if (cachedMqttClient != null) {
            if (cachedMqttClient.mqttClient().isPresent()) {
                IMqttClient iMqttClient = cachedMqttClient.mqttClient().get();
                final var nrOfPendingDeliveryTokens = iMqttClient.getPendingDeliveryTokens().length;
                final var pendingDeliveryTokens = new ArrayList<PendingDeliveryToken>();
                Arrays.stream(iMqttClient.getPendingDeliveryTokens()).forEach(token -> {
                    try {
                        final var messageId = token.getMessageId();
                        final var grantedQos = token.getGrantedQos();
                        final var topics = token.getTopics();
                        final var complete = token.isComplete();
                        final var message = token.getMessage();
                        int qos = message.getQos();
                        pendingDeliveryTokens.add(new PendingDeliveryToken(messageId, grantedQos, topics, complete, qos));
                    } catch (Exception e) {
                        LOGGER.error("Error while fetching the technical state of the MQTT client for endpoint with the MQTT client ID '{}'. Skipping this one.", onboardingResponse.getConnectionCriteria().getClientId());
                    }
                });
                return new TechnicalConnectionState(nrOfPendingDeliveryTokens, application.usesRouterDevice(), pendingDeliveryTokens, cachedMqttClient.connectionErrors());
            }
        }
        LOGGER.debug("Did not find a mqtt client for endpoint with the MQTT client ID '{}'.", onboardingResponse.getConnectionCriteria().getClientId());
        return new TechnicalConnectionState(0, false, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Get all pending delivery tokens for the endpoint.
     *
     * @param onboardingResponse The onboarding response.
     * @return The list of pending delivery tokens.
     */
    public int getNumberOfPendingDeliveryTokens(OnboardingResponse onboardingResponse) {
        final var cachedMqttClient = cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
        if (cachedMqttClient != null) {
            if (cachedMqttClient.mqttClient().isPresent()) {
                IMqttClient iMqttClient = cachedMqttClient.mqttClient().get();
                return iMqttClient.getPendingDeliveryTokens().length;
            }
        }
        return 0;
    }

    /**
     * Get all pending delivery tokens for the endpoint.
     *
     * @param onboardingResponse The onboarding response.
     * @return The list of pending delivery tokens.
     */
    public List<IMqttDeliveryToken> getPendingDeliveryTokens(OnboardingResponse onboardingResponse) {
        final var cachedMqttClient = cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
        if (cachedMqttClient != null) {
            if (cachedMqttClient.mqttClient().isPresent()) {
                IMqttClient iMqttClient = cachedMqttClient.mqttClient().get();
                return Arrays.asList(iMqttClient.getPendingDeliveryTokens());
            }
        }
        return Collections.emptyList();
    }

    /**
     * Disconnect an existing connection.
     *
     * @param onboardingResponse -
     */
    public void disconnect(OnboardingResponse onboardingResponse) {
        final var cachedMqttClient = cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
        if (null != cachedMqttClient) {
            cachedMqttClient.mqttClient().ifPresent(iMqttClient -> {
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
        cachedMqttClients.values().removeIf(cachedMqttClient -> cachedMqttClient.mqttClient().isEmpty() || !cachedMqttClient.mqttClient().get().isConnected());
    }

    /**
     * Clear the connection errors.
     *
     * @param endpoint The endpoint.
     */
    public void clearConnectionErrors(Endpoint endpoint) {
        final var cachedMqttClient = getCachedMqttClient(endpoint.asOnboardingResponse());
        if (null != cachedMqttClient) {
            cachedMqttClient.clearConnectionErrors();
        }
        cachedMqttClients.put(endpoint.asOnboardingResponse().getConnectionCriteria().getClientId(), cachedMqttClient);
    }

}
