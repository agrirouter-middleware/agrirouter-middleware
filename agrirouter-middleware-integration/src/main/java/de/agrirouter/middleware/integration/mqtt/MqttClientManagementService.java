package de.agrirouter.middleware.integration.mqtt;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.Gateway;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.exception.CouldNotCreateMqttClientException;
import com.dke.data.agrirouter.convenience.mqtt.client.MqttOptionService;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.events.CheckConnectionsEvent;
import de.agrirouter.middleware.domain.Endpoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.time.Instant;
import java.util.*;

/**
 * Centralized management for all the applications.
 */
@Slf4j
@Service
@ApplicationScope
public class MqttClientManagementService {

    private final Map<String, CachedMqttClient> cachedMqttClients;
    private final MqttOptionService mqttOptionService;
    private final MessageHandlingCallback messageHandlingCallback;
    private final MqttStatistics mqttStatistics;
    private final Environment environment;

    public MqttClientManagementService(MqttOptionService mqttOptionService,
                                       MessageHandlingCallback messageHandlingCallback,
                                       MqttStatistics mqttStatistics,
                                       Environment environment) {
        this.mqttOptionService = mqttOptionService;
        this.messageHandlingCallback = messageHandlingCallback;
        this.mqttStatistics = mqttStatistics;
        this.environment = environment;
        this.cachedMqttClients = new HashMap<>();
    }

    /**
     * Get or create an MQTT client for the given onboard response.
     * The client will be connected to the AR and subscribes to the available topics.
     *
     * @param endpoint The endpoint for which the MQTT client should be created.
     * @return The MQTT client.
     */
    public Optional<IMqttClient> get(Endpoint endpoint) {
        OnboardingResponse onboardingResponse = null;
        try {
            onboardingResponse = endpoint.asOnboardingResponse();
        } catch (BusinessException e) {
            log.error("Could not get onboarding response for endpoint '{}'.", endpoint, e);
        }

        if (onboardingResponse != null) {
            if (Gateway.MQTT.getKey().equals(onboardingResponse.getConnectionCriteria().getGatewayId())) {
                final CachedMqttClient cachedMqttClient = getCachedMqttClient(onboardingResponse);
                IMqttClient mqttClientAfterInitialization = null;
                if (!isConnected(cachedMqttClient)) {
                    try {
                        log.debug("The existing mqtt client connection for endpoint with the MQTT client ID '{}' is no longer connected, therefore removing this one from the cache and reconnecting the endpoint.", onboardingResponse.getConnectionCriteria().getClientId());
                        cachedMqttClients.remove(cachedMqttClient.id());
                        final var mqttClient = initMqttClient(onboardingResponse);
                        final var newCachedMqttClient = new CachedMqttClient(onboardingResponse.getSensorAlternateId(), onboardingResponse.getConnectionCriteria().getClientId(), Optional.of(mqttClient), cachedMqttClient.connectionErrors());
                        cachedMqttClients.put(onboardingResponse.getConnectionCriteria().getClientId(), newCachedMqttClient);
                        mqttClientAfterInitialization = mqttClient;
                    } catch (Exception e) {
                        cachedMqttClient.connectionErrors().add(new ConnectionError(Instant.now(), String.format("There was an error while connecting the client, the error message was '%s'.", e.getMessage())));
                        cachedMqttClients.put(onboardingResponse.getConnectionCriteria().getClientId(), cachedMqttClient);
                    }
                } else {
                    log.debug("Returning existing mqtt client for endpoint with the MQTT client ID '{}'.", onboardingResponse.getConnectionCriteria().getClientId());
                    //noinspection OptionalGetWithoutIsPresent
                    mqttClientAfterInitialization = cachedMqttClient.mqttClient().get();
                }
                if (null != mqttClientAfterInitialization) {
                    if (mqttClientAfterInitialization.isConnected()) {
                        return Optional.of(mqttClientAfterInitialization);
                    } else {
                        log.error("The mqtt client for endpoint '{}' with the MQTT client ID '{}' is not connected.", onboardingResponse.getSensorAlternateId(), onboardingResponse.getConnectionCriteria().getClientId());
                    }
                }
            } else {
                log.debug("This onboard response is not MQTT ready, the gateway is set to {}.", onboardingResponse.getConnectionCriteria().getGatewayId());
            }
        }
        return Optional.empty();
    }

    private boolean isConnected(CachedMqttClient cachedMqttClient) {
        return cachedMqttClient.mqttClient().isPresent() && cachedMqttClient.mqttClient().get().isConnected();
    }

    private CachedMqttClient getCachedMqttClient(OnboardingResponse onboardingResponse) {
        final var cachedMqttClient = cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
        if (null == cachedMqttClient) {
            mqttStatistics.increaseNumberOfCacheMisses();
            log.debug("Did not find a mqtt client for endpoint with the MQTT client ID '{}'. Creating a new one.", onboardingResponse.getConnectionCriteria().getClientId());
            final var newCachedMqttClient = new CachedMqttClient(onboardingResponse.getSensorAlternateId(), onboardingResponse.getConnectionCriteria().getClientId(), Optional.empty(), new ArrayList<>());
            cachedMqttClients.put(onboardingResponse.getConnectionCriteria().getClientId(), newCachedMqttClient);
        }
        return cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
    }

    private IMqttClient initMqttClient(OnboardingResponse onboardingResponse) throws MqttException {
        mqttStatistics.increaseNumberOfClientInitializations();
        IMqttClient mqttClient = createMqttClient(onboardingResponse);
        final var mqttConnectOptions = mqttOptionService.createMqttConnectOptions(onboardingResponse);
        mqttClient.connect(mqttConnectOptions);
        mqttClient.subscribe(onboardingResponse.getConnectionCriteria().getCommands());
        mqttClient.setCallback(messageHandlingCallback);
        return mqttClient;
    }

    private IMqttClient createMqttClient(OnboardingResponse onboardingResponse) {
        try {
            var host = onboardingResponse.getConnectionCriteria().getHost();
            var port = onboardingResponse.getConnectionCriteria().getPort();
            var clientId = onboardingResponse.getConnectionCriteria().getClientId();
            if (StringUtils.isAnyBlank(host, port, clientId)) {
                throw new CouldNotCreateMqttClientException("Currently there are parameters missing. Did you onboard correctly - host, port or client id are missing.");
            } else {
                return new MqttClient(this.environment.getMqttServerUrl(host, port), Objects.requireNonNull(clientId), new MemoryPersistence());
            }
        } catch (MqttException var5) {
            throw new CouldNotCreateMqttClientException("Could not create MQTT client.", var5);
        }
    }

    /**
     * Get the state of a MQTT connection.
     *
     * @param endpoint The endpoint.
     * @return The connection state.
     */
    public ConnectionState getState(Endpoint endpoint) {
        try {
            var onboardingResponse = endpoint.asOnboardingResponse();
            final var cachedMqttClient = cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
            return new ConnectionState(cachedMqttClient != null ? cachedMqttClient.id() : null, cachedMqttClient != null,
                    cachedMqttClient != null && cachedMqttClient.mqttClient().isPresent() && cachedMqttClient.mqttClient().get().isConnected(),
                    cachedMqttClient != null ? cachedMqttClient.connectionErrors() : Collections.emptyList());
        } catch (BusinessException e) {
            log.error(e.getErrorMessage().asLogMessage());
            return new ConnectionState(null, false, false, Collections.emptyList());
        }
    }


    /**
     * Determine the technical connection state.
     *
     * @param endpoint -
     * @return -
     */
    public TechnicalConnectionState getTechnicalState(Endpoint endpoint) {
        try {
            var onboardingResponse = endpoint.asOnboardingResponse();
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
                            log.error("Error while fetching the technical state of the MQTT client for endpoint with the MQTT client ID '{}'. Skipping this one.", onboardingResponse.getConnectionCriteria().getClientId());
                        }
                    });
                    return new TechnicalConnectionState(nrOfPendingDeliveryTokens, pendingDeliveryTokens, cachedMqttClient.connectionErrors());
                }
            }
        } catch (BusinessException e) {
            log.error(e.getErrorMessage().asLogMessage());
        }
        return new TechnicalConnectionState(0, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Get all pending delivery tokens for the endpoint.
     *
     * @param endpoint The endpoint.
     * @return The list of pending delivery tokens.
     */
    public List<IMqttDeliveryToken> getPendingDeliveryTokens(Endpoint endpoint) {
        try {
            final var onboardingResponse = endpoint.asOnboardingResponse();
            final var cachedMqttClient = cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
            if (cachedMqttClient != null) {
                if (cachedMqttClient.mqttClient().isPresent()) {
                    IMqttClient iMqttClient = cachedMqttClient.mqttClient().get();
                    return Arrays.asList(iMqttClient.getPendingDeliveryTokens());
                }
            }
        } catch (BusinessException e) {
            log.error(e.getErrorMessage().asLogMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Disconnect an existing connection.
     *
     * @param endpoint The endpoint.
     */
    public void disconnect(Endpoint endpoint) {
        mqttStatistics.increaseNumberOfDisconnects();
        try {
            var onboardingResponse = endpoint.asOnboardingResponse();
            disconnect(onboardingResponse);
        } catch (BusinessException e) {
            log.error(e.getErrorMessage().asLogMessage());
        }
    }

    /**
     * Disconnect an existing connection.
     *
     * @param endpoint The endpoint.
     */
    public void disconnectForOriginalOnboardingResponse(Endpoint endpoint) {
        mqttStatistics.increaseNumberOfDisconnects();
        try {
            var onboardingResponse = endpoint.asOnboardingResponse(true);
            disconnect(onboardingResponse);
        } catch (BusinessException e) {
            log.error(e.getErrorMessage().asLogMessage());
        }
    }

    private void disconnect(OnboardingResponse onboardingResponse) {
        final var cachedMqttClient = cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
        if (null != cachedMqttClient) {
            cachedMqttClient.mqttClient().ifPresent(iMqttClient -> {
                try {
                    cachedMqttClients.remove(onboardingResponse.getConnectionCriteria().getClientId());
                    iMqttClient.disconnect();
                } catch (MqttException e) {
                    log.error("Could not disconnect the MQTT client.", e);
                }
            });
        }
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

    /**
     * Count the number of active connections.
     *
     * @return The number of active connections.
     */
    public long getNumberOfActiveConnections() {
        return cachedMqttClients.values().stream().filter(cachedMqttClient -> cachedMqttClient.mqttClient().isPresent() && cachedMqttClient.mqttClient().get().isConnected()).count();
    }

    /**
     * Count the number of inactive connections.
     *
     * @return The number of inactive connections.
     */
    public long getNumberOfInactiveConnections() {
        return cachedMqttClients.values().stream().filter(cachedMqttClient -> cachedMqttClient.mqttClient().isEmpty() || !cachedMqttClient.mqttClient().get().isConnected()).count();
    }

    /**
     * Remove all stale connections.
     */
    @EventListener(CheckConnectionsEvent.class)
    public void removeAllStaleConnections() {
        log.info("There has been a check connections event. Checking all connections.");
        var disconnectedMqttClients = cachedMqttClients.values().stream().filter(cachedMqttClient -> cachedMqttClient.mqttClient().isEmpty() || !cachedMqttClient.mqttClient().get().isConnected());
        disconnectedMqttClients.forEach(cachedMqttClient -> {
            log.info("Removing stale connection with MQTT client ID '{}'.", cachedMqttClient.id());
            cachedMqttClients.remove(cachedMqttClient.id());
        });
    }

}
