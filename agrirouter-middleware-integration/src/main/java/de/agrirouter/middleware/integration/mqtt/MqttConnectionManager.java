package de.agrirouter.middleware.integration.mqtt;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.CertificationType;
import com.dke.data.agrirouter.api.env.Constants;
import com.dke.data.agrirouter.api.exception.CouldNotCreateMqttClientException;
import com.dke.data.agrirouter.impl.common.ssl.KeyStoreCreationService;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.RouterDevice;
import de.agrirouter.middleware.integration.mqtt.status.MqttConnectionStatus;
import de.agrirouter.middleware.persistence.jpa.ApplicationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManagerFactory;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
@Slf4j
@Component
@RequiredArgsConstructor
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MqttConnectionManager {

    private final Map<String, CachedMqttClient> cachedMqttClients = new HashMap<>();

    private final ApplicationRepository applicationRepository;
    private final MqttStatistics mqttStatistics;
    private final ApplicationContext applicationContext;
    private final SubscriptionsForMqttClient subscriptionsForMqttClient;

    private final KeyStoreCreationService keyStoreCreationService = new KeyStoreCreationService();

    @Value("${app.agrirouter.mqtt.options.clean-session}")
    private boolean cleanSession;

    @Value("${app.agrirouter.mqtt.options.keep-alive-interval}")
    private int keepAliveInterval;

    @Value("${app.agrirouter.mqtt.options.connection-timeout}")
    private int connectionTimeout;

    @PostConstruct
    public void connectAllExistingRouterDevices() {
        applicationRepository.findAll().forEach(application -> {
            var existingRouterDevice = application.getApplicationSettings().getRouterDevice();
            if (existingRouterDevice != null) {
                log.info("Connecting router device for application: {}", application.getApplicationId());
                try {
                    var mqttClient = initMqttClient(existingRouterDevice);
                    log.debug("Connected router device for application: {}", application.getApplicationId());
                    mqttStatistics.increaseNumberOfConnects();
                    final var newCachedMqttClient = new CachedMqttClient(existingRouterDevice.getDeviceAlternateId(), existingRouterDevice.getConnectionCriteria().getClientId(), Optional.of(mqttClient), new ArrayList<>());
                    cachedMqttClients.put(existingRouterDevice.getConnectionCriteria().getClientId(), newCachedMqttClient);
                    log.debug("Cached MQTT client for application has been created and is ready to be used: {}", application.getApplicationId());
                } catch (Exception e) {
                    log.error("Could not connect router device for application: {}", application.getApplicationId(), e);
                    throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(application.getApplicationId()));
                }
            } else {
                log.warn("Router device not found for application: {}", application.getApplicationId());
            }
        });
    }

    public void connectNewlyAddedRouterDevices() {
        applicationRepository.findAll().forEach(application -> {
            var existingRouterDevice = application.getApplicationSettings().getRouterDevice();
            if (existingRouterDevice != null) {
                var cachedMqttClient = cachedMqttClients.get(existingRouterDevice.getConnectionCriteria().getClientId());
                if (null != cachedMqttClient && cachedMqttClient.mqttClient().isPresent()) {
                    log.debug("Router device already connected for application: {}", application.getApplicationId());
                } else {
                    log.info("Connecting router device for application: {}", application.getApplicationId());
                    try {
                        var mqttClient = initMqttClient(existingRouterDevice);
                        log.debug("Connected router device for application: {}", application.getApplicationId());
                        mqttStatistics.increaseNumberOfConnects();
                        final var newCachedMqttClient = new CachedMqttClient(existingRouterDevice.getDeviceAlternateId(), existingRouterDevice.getConnectionCriteria().getClientId(), Optional.of(mqttClient), new ArrayList<>());
                        cachedMqttClients.put(existingRouterDevice.getConnectionCriteria().getClientId(), newCachedMqttClient);
                        log.debug("Cached MQTT client for application has been created and is ready to be used: {}", application.getApplicationId());
                    } catch (Exception e) {
                        log.error("Could not connect router device for application: {}", application.getApplicationId(), e);
                        throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(application.getApplicationId()));
                    }
                }
            } else {
                log.warn("Router device not found for application: {}", application.getApplicationId());
            }
        });
    }

    CachedMqttClient getCachedMqttClient(OnboardingResponse onboardingResponse) {
        final var cachedMqttClient = cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
        if (null == cachedMqttClient) {
            mqttStatistics.increaseNumberOfCacheMisses();
            log.debug("Did not find a mqtt client for endpoint with the MQTT client ID '{}'. Creating a new one.", onboardingResponse.getConnectionCriteria().getClientId());
            final var newCachedMqttClient = new CachedMqttClient(onboardingResponse.getSensorAlternateId(), onboardingResponse.getConnectionCriteria().getClientId(), Optional.empty(), new ArrayList<>());
            cachedMqttClients.put(onboardingResponse.getConnectionCriteria().getClientId(), newCachedMqttClient);
        }
        var existingCachedMqttClient = cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
        existingCachedMqttClient.mqttClient().ifPresent(mqttClient -> subscribeIfNecessary(onboardingResponse, mqttClient));
        return existingCachedMqttClient;
    }

    private void subscribeIfNecessary(OnboardingResponse onboardingResponse, Mqtt3AsyncClient mqttClient) {
        var topic = onboardingResponse.getConnectionCriteria().getCommands();
        var clientId = mqttClient.getConfig().getClientIdentifier().map(Object::toString).orElse("n.a.");
        log.debug("Checking if the subscriptions for endpoint '{}' are already sent.", onboardingResponse.getSensorAlternateId());
        log.debug("The topic for the incoming commands is '{}'.", topic);
        if (subscriptionsForMqttClient.exists(clientId, topic)) {
            log.debug("Already sent subscriptions for endpoint '{}', not sending again.", onboardingResponse.getSensorAlternateId());
        } else {
            log.debug("Sending subscriptions for endpoint '{}'.", onboardingResponse.getSensorAlternateId());
            mqttClient.subscribeWith()
                    .topicFilter(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .send()
                    .whenComplete((subAck, throwable) -> {
                        if (throwable == null) {
                            subscriptionsForMqttClient.add(clientId, topic);
                            log.debug("Successfully subscribed to the commands for endpoint '{}'.", onboardingResponse.getSensorAlternateId());
                        } else {
                            log.error("Could not subscribe to the commands for endpoint '{}'.", onboardingResponse.getSensorAlternateId(), throwable);
                        }
                    });
        }
    }

    private Mqtt3AsyncClient initMqttClient(RouterDevice endpoint) {
        try {
            mqttStatistics.increaseNumberOfClientInitializations();
            var messageHandlingCallback = applicationContext.getBean(MessageHandlingCallback.class);
            messageHandlingCallback.setClientIdOfTheRouterDevice(endpoint.getConnectionCriteria().getClientId());
            var mqttClient = createMqttClient(endpoint, messageHandlingCallback);
            mqttClient.publishes(MqttGlobalPublishFilter.ALL, messageHandlingCallback::handleMessage);
            mqttClient.connect(
                    Mqtt3Connect.builder()
                            .cleanSession(cleanSession)
                            .keepAlive(keepAliveInterval)
                            .build()
            ).get(connectionTimeout, TimeUnit.SECONDS);
            return mqttClient;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CouldNotCreateMqttClientException("Connection to MQTT broker was interrupted.", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new CouldNotCreateMqttClientException("Could not connect to MQTT broker within timeout.", e);
        }
    }

    private Mqtt3AsyncClient createMqttClient(RouterDevice routerDevice, MessageHandlingCallback messageHandlingCallback) {
        var host = routerDevice.getConnectionCriteria().getHost();
        var port = routerDevice.getConnectionCriteria().getPort();
        var clientId = routerDevice.getConnectionCriteria().getClientId();
        if (StringUtils.isAnyBlank(host, port, clientId)) {
            throw new CouldNotCreateMqttClientException("Currently there are parameters missing. Did you onboard correctly - host, port or client id are missing.");
        }
        var sslConfig = MqttClientSslConfig.builder()
                .keyManagerFactory(createKeyManagerFactory(routerDevice))
                .build();
        return Mqtt3Client.builder()
                .identifier(clientId)
                .serverHost(host)
                .serverPort(Integer.parseInt(port))
                .sslConfig(sslConfig)
                .addConnectedListener((MqttClientConnectedListener) context ->
                        messageHandlingCallback.connectComplete(host))
                .addDisconnectedListener((MqttClientDisconnectedListener) context ->
                        messageHandlingCallback.connectionLost(context.getCause().orElse(null)))
                .buildAsync();
    }

    private KeyManagerFactory createKeyManagerFactory(RouterDevice routerDevice) {
        try {
            var certificate = routerDevice.getAuthentication().getCertificate();
            var secret = routerDevice.getAuthentication().getSecret();
            var certificationType = CertificationType.valueOf(routerDevice.getAuthentication().getType());
            var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            if (certificationType == CertificationType.PEM) {
                kmf.init(
                        keyStoreCreationService.createAndReturnKeystoreFromPEM(certificate, secret),
                        Constants.DEFAULT_PASSWORD.toCharArray());
            } else if (certificationType == CertificationType.P12) {
                kmf.init(
                        keyStoreCreationService.createAndReturnKeystoreFromP12(certificate, secret),
                        secret.toCharArray());
            } else {
                throw new CouldNotCreateMqttClientException("Unsupported certificate type: " + certificationType);
            }
            return kmf;
        } catch (CouldNotCreateMqttClientException e) {
            throw e;
        } catch (Exception e) {
            throw new CouldNotCreateMqttClientException("Could not create SSL context for MQTT client.", e);
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
                return new TechnicalConnectionState(0, Collections.emptyList(), cachedMqttClient.connectionErrors());
            }
        } catch (BusinessException e) {
            log.error(e.getErrorMessage().asLogMessage());
        }
        return new TechnicalConnectionState(0, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Get all pending delivery tokens for the endpoint.
     * HiveMQ does not expose pending delivery tokens; an empty list is always returned.
     *
     * @param endpoint The endpoint.
     * @return An empty list.
     */
    public List<?> getPendingDeliveryTokens(Endpoint endpoint) {
        return Collections.emptyList();
    }

    /**
     * Clear the connection errors.
     *
     * @param endpoint The endpoint.
     */
    void clearConnectionErrors(Endpoint endpoint) {
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
    long getNumberOfActiveConnections() {
        return cachedMqttClients.values().stream().filter(cachedMqttClient -> cachedMqttClient.mqttClient().isPresent() && cachedMqttClient.mqttClient().get().getState().isConnected()).count();
    }

    /**
     * Count the number of inactive connections.
     *
     * @return The number of inactive connections.
     */
    long getNumberOfInactiveConnections() {
        return cachedMqttClients.values().stream().filter(cachedMqttClient -> cachedMqttClient.mqttClient().isEmpty() || !cachedMqttClient.mqttClient().get().getState().isConnected()).count();
    }

    /**
     * Get the connection status for all MQTT clients.
     *
     * @return The connection status for all MQTT clients.
     */
    List<MqttConnectionStatus> getMqttConnectionStatus() {
        var mqttConnectionStatus = new ArrayList<MqttConnectionStatus>();
        cachedMqttClients.forEach((key, cachedMqttClient) -> {
            if (cachedMqttClient.mqttClient().isPresent()) {
                var mqttClient = cachedMqttClient.mqttClient().get();
                var clientId = mqttClient.getConfig().getClientIdentifier().map(Object::toString).orElse("n.a.");
                var status = mqttClient.getState().isConnected() ? "CONNECTED" : "DISCONNECTED";
                mqttConnectionStatus.add(MqttConnectionStatus.builder().key(key).clientId(clientId).connectionStatus(status).build());
            } else {
                mqttConnectionStatus.add(MqttConnectionStatus.builder().key(key).clientId("n.a.").connectionStatus("EMPTY").build());
            }
        });
        return mqttConnectionStatus;
    }

    /**
     * Get the state of a MQTT connection.
     *
     * @param endpoint The endpoint.
     * @return The connection state.
     */
    ConnectionState getState(Endpoint endpoint) {
        try {
            var onboardingResponse = endpoint.asOnboardingResponse();
            final var cachedMqttClient = cachedMqttClients.get(onboardingResponse.getConnectionCriteria().getClientId());
            if (null != cachedMqttClient) {
                if (cachedMqttClient.mqttClient().isPresent()) {
                    var mqttClient = cachedMqttClient.mqttClient().get();
                    var clientId = mqttClient.getConfig().getClientIdentifier().map(Object::toString).orElse("n.a.");
                    return new ConnectionState(cachedMqttClient.id(),
                            true,
                            mqttClient.getState().isConnected(),
                            subscriptionsForMqttClient.exists(clientId, onboardingResponse.getConnectionCriteria().getCommands()),
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
     * Clears all subscriptions associated with the provided MQTT client ID.
     *
     * @param clientId The unique identifier of the MQTT client whose subscriptions are to be cleared.
     */
    public void clearSubscriptionsForMqttClient(String clientId) {
        subscriptionsForMqttClient.clear(clientId);
    }
}
