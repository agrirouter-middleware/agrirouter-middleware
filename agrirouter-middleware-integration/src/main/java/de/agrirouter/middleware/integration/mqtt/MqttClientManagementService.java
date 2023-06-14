package de.agrirouter.middleware.integration.mqtt;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.Gateway;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.exception.CouldNotCreateMqttClientException;
import com.dke.data.agrirouter.convenience.mqtt.client.MqttOptionService;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.mqtt.status.MqttConnectionStatus;
import de.agrirouter.middleware.persistence.ApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Centralized management for all the applications.
 */
@Slf4j
@Service
public class MqttClientManagementService {

    private final Map<String, CachedMqttClient> cachedMqttClients;
    private final MqttOptionService mqttOptionService;
    private final MqttStatistics mqttStatistics;
    private final Environment environment;
    private final ApplicationRepository applicationRepository;
    private final ApplicationContext applicationContext;
    private final SubscriptionsForMqttClient subscriptionsForMqttClient;

    public MqttClientManagementService(MqttOptionService mqttOptionService,
                                       MqttStatistics mqttStatistics,
                                       Environment environment,
                                       ApplicationRepository applicationRepository,
                                       ApplicationContext applicationContext,
                                       SubscriptionsForMqttClient subscriptionsForMqttClient) {
        this.mqttOptionService = mqttOptionService;
        this.mqttStatistics = mqttStatistics;
        this.environment = environment;
        this.applicationRepository = applicationRepository;
        this.applicationContext = applicationContext;
        this.subscriptionsForMqttClient = subscriptionsForMqttClient;
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
                        final var mqttClient = initMqttClient(endpoint);
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
                        subscribeIfNecessary(onboardingResponse, mqttClientAfterInitialization);
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

    private void subscribeIfNecessary(OnboardingResponse onboardingResponse, IMqttClient mqttClient) {
        var topic = onboardingResponse.getConnectionCriteria().getCommands();
        try {
            if (subscriptionsForMqttClient.exists(mqttClient.getClientId(), topic)) {
                log.debug("Already sent subscriptions for endpoint '{}', not sending again.", onboardingResponse.getSensorAlternateId());
            } else {
                log.debug("Sending subscriptions for endpoint '{}'.", onboardingResponse.getSensorAlternateId());
                var iMqttToken = mqttClient.subscribeWithResponse(topic);
                if (iMqttToken.isComplete()) {
                    subscriptionsForMqttClient.add(mqttClient.getClientId(), topic);
                    log.debug("Successfully subscribed to the commands for endpoint '{}'.", onboardingResponse.getSensorAlternateId());
                } else {
                    log.error("Could not subscribe to the commands for endpoint '{}'.", onboardingResponse.getSensorAlternateId());
                }
            }
        } catch (MqttException e) {
            log.error("Could not subscribe to the commands for endpoint '{}'.", onboardingResponse.getSensorAlternateId(), e);
        }
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

    private IMqttClient initMqttClient(Endpoint endpoint) throws MqttException {
        mqttStatistics.increaseNumberOfClientInitializations();
        final var mqttClient = createMqttClient(endpoint);
        var onboardingResponse = endpoint.asOnboardingResponse();
        final var mqttConnectOptions = mqttOptionService.createMqttConnectOptions(onboardingResponse);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setKeepAliveInterval(30);
        mqttConnectOptions.setConnectionTimeout(120);
        mqttClient.connect(mqttConnectOptions);
        var messageHandlingCallback = applicationContext.getBean(MessageHandlingCallback.class);
        messageHandlingCallback.setMqttClient(mqttClient);
        messageHandlingCallback.setClientIdOfTheRouterDevice(onboardingResponse.getConnectionCriteria().getClientId());
        mqttClient.setCallback(messageHandlingCallback);
        return mqttClient;
    }

    private IMqttClient createMqttClient(Endpoint endpoint) {
        try {
            var onboardingResponse = endpoint.asOnboardingResponse();
            var host = onboardingResponse.getConnectionCriteria().getHost();
            var port = onboardingResponse.getConnectionCriteria().getPort();
            final String clientId;
            if (!endpoint.usesRouterDevice()) {
                log.error("The endpoint '{}' does not use a router device, therefore the client ID will be set to the original client ID. This should not be the case.", endpoint);
                throw new BusinessException(ErrorMessageFactory.missingRouterDevice(endpoint.getExternalEndpointId()));
            } else {
                log.debug("The endpoint '{}' uses a router device, therefore the client ID will be set.", endpoint);
                Optional<Application> optionalApplication = applicationRepository.findByEndpointsContains(endpoint);
                if (optionalApplication.isPresent()) {
                    log.debug("We are using a unique client ID to avoid problems in case the router device has been used multiple times.");
                    clientId = MqttClient.generateClientId();
                } else {
                    log.debug("The endpoint '{}' does not belong to an application, therefore the client ID will be set to the original client ID.", endpoint);
                    throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
                }
            }
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
            if (null != cachedMqttClient) {
                if (cachedMqttClient.mqttClient().isPresent()) {
                    var iMqttClient = cachedMqttClient.mqttClient().get();
                    return new ConnectionState(cachedMqttClient.id(),
                            true,
                            iMqttClient.isConnected(),
                            subscriptionsForMqttClient.exists(iMqttClient.getClientId(), onboardingResponse.getConnectionCriteria().getCommands()),
                            cachedMqttClient.connectionErrors());
                } else {
                    return new ConnectionState(cachedMqttClient.id(),
                            true,
                            false,
                            false, cachedMqttClient.connectionErrors());
                }
            }
        } catch (BusinessException e) {
            log.error(e.getErrorMessage().asLogMessage());
            return new ConnectionState("n.a.", false, false, false, Collections.emptyList());
        }
        return new ConnectionState("n.a.", false, false, false, Collections.emptyList());
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
     * Get the connection status for all MQTT clients.
     *
     * @return The connection status for all MQTT clients.
     */
    public List<MqttConnectionStatus> getMqttConnectionStatus() {
        var mqttConnectionStatus = new ArrayList<MqttConnectionStatus>();
        cachedMqttClients.forEach((key, cachedMqttClient) -> {
            if (cachedMqttClient.mqttClient().isPresent()) {
                var iMqttClient = cachedMqttClient.mqttClient().get();
                var status = iMqttClient.isConnected() ? "CONNECTED" : "DISCONNECTED";
                mqttConnectionStatus.add(MqttConnectionStatus.builder().key(key).clientId(iMqttClient.getClientId()).connectionStatus(status).build());
            } else {
                mqttConnectionStatus.add(MqttConnectionStatus.builder().key(key).clientId("n.a.").connectionStatus("EMPTY").build());
            }
        });
        return mqttConnectionStatus;
    }

    /**
     * Disconnect and remove the MQTT client from the cache.
     *
     * @param clientId The client ID.
     */
    public void kill(String clientId) {
        var cachedMqttClient = cachedMqttClients.get(clientId);
        if (cachedMqttClient != null) {
            cachedMqttClient.mqttClient().ifPresent(iMqttClient -> {
                try {
                    log.warn("Remove the existing callback to prevent looping.");
                    iMqttClient.setCallback(null);
                    log.warn("Disconnecting the client, remove it from the cache and clear the former subscriptions. Next connect will be done, when there is an endpoint asking for it.");
                    iMqttClient.disconnectForcibly();
                    cachedMqttClients.remove(clientId);
                    subscriptionsForMqttClient.clear(clientId);
                } catch (MqttException e) {
                    log.error("Could not disconnect the MQTT client for client ID {}.", clientId, e);
                }
            });
        }
    }

}
