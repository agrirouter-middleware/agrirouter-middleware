package de.agrirouter.middleware.integration.mqtt;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.exception.CouldNotCreateMqttClientException;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
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
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
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
                } catch (BusinessException e) {
                    log.warn("Could not connect router device for application '{}', skipping: {}", application.getApplicationId(), e.getErrorMessage().asLogMessage());
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
                    } catch (BusinessException e) {
                        log.warn("Could not connect router device for application '{}', skipping: {}", application.getApplicationId(), e.getErrorMessage().asLogMessage());
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
        log.debug("Checking if the subscriptions for endpoint '{}' are already sent.", onboardingResponse.getSensorAlternateId());
        log.debug("The topic for the incoming commands is '{}'.", topic);
        if (subscriptionsForMqttClient.exists(mqttClient.getConfig().getClientIdentifier().get().toString(), topic)) {
            log.debug("Already sent subscriptions for endpoint '{}', not sending again.", onboardingResponse.getSensorAlternateId());
        } else {
            log.debug("Sending subscriptions for endpoint '{}'.", onboardingResponse.getSensorAlternateId());
            final var subscribeFuture = mqttClient.subscribeWith()
                    .topicFilter(topic)
                    // TODO add QoS
                    .send();

            subscribeFuture.whenComplete((subAck, throwable) -> {
                if (throwable != null) {
                    log.error("Could not subscribe to the commands for endpoint '{}'.", onboardingResponse.getSensorAlternateId(), throwable);
                } else {
                    subscriptionsForMqttClient.add(mqttClient.getConfig().getClientIdentifier().get().toString(), topic);
                    log.debug("Successfully subscribed to the commands for endpoint '{}'.", onboardingResponse.getSensorAlternateId());
                }
            });

            try {
                subscribeFuture.get(connectionTimeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Subscription attempt interrupted for endpoint '{}'.", onboardingResponse.getSensorAlternateId(), e);
            } catch (ExecutionException | TimeoutException e) {
                log.error("Could not subscribe to the commands for endpoint '{}'.", onboardingResponse.getSensorAlternateId(), e);
            }
        }
    }

    private Mqtt3AsyncClient initMqttClient(RouterDevice endpoint) {
        mqttStatistics.increaseNumberOfClientInitializations();
        final var mqttClient = createMqttClient(endpoint);
        var messageHandlingCallback = applicationContext.getBean(MessageHandlingCallback.class);

        mqttClient.publishes(com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL, messageHandlingCallback);

        final var connectFuture = mqttClient.connectWith()
                .cleanSession(cleanSession)
                .keepAlive(keepAliveInterval)
                .send();

        try {
            connectFuture.get(connectionTimeout, TimeUnit.SECONDS);
            log.info("Successfully connected MQTT client for application: {}", endpoint.getConnectionCriteria().getClientId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Connection attempt interrupted for MQTT client: {}", endpoint.getConnectionCriteria().getClientId(), e);
            throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.getConnectionCriteria().getClientId()), e);
        } catch (ExecutionException | TimeoutException e) {
            log.error("Could not connect MQTT client for application: {}", endpoint.getConnectionCriteria().getClientId(), e);
            throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.getConnectionCriteria().getClientId()), e);
        }

        return mqttClient;
    }

    private Mqtt3AsyncClient createMqttClient(RouterDevice onboardingResponse) {
        var host = onboardingResponse.getConnectionCriteria().getHost();
        var port = onboardingResponse.getConnectionCriteria().getPort();
        var clientId = onboardingResponse.getConnectionCriteria().getClientId();
        if (StringUtils.isAnyBlank(host, port, clientId)) {
            throw new CouldNotCreateMqttClientException("Currently there are parameters missing. Did you onboard correctly - host, port or client id are missing.");
        } else {
            try {
                return Mqtt3Client.builder()
                        .identifier(clientId)
                        .serverHost(host)
                        .serverPort(Integer.parseInt(port))
                        .sslConfig(createMqttClientSslConfig(onboardingResponse.getAuthentication()))
                        .buildAsync();
            } catch (Exception e) {
                throw new CouldNotCreateMqttClientException("Could not create MQTT client.", e);
            }
        }
    }

    private MqttClientSslConfig createMqttClientSslConfig(de.agrirouter.middleware.domain.Authentication authentication) throws Exception {
        var keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new ByteArrayInputStream(Base64.getDecoder().decode(authentication.getCertificate())), authentication.getSecret().toCharArray());

        var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, authentication.getSecret().toCharArray());

        var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);

        return MqttClientSslConfig.builder()
                .keyManagerFactory(keyManagerFactory)
                .trustManagerFactory(trustManagerFactory)
                .build();
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
                var iMqttClient = cachedMqttClient.mqttClient().get();
                var status = iMqttClient.getState().isConnected() ? "CONNECTED" : "DISCONNECTED";
                mqttConnectionStatus.add(MqttConnectionStatus.builder().key(key).clientId(iMqttClient.getConfig().getClientIdentifier().get().toString()).connectionStatus(status).build());
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
                    var iMqttClient = cachedMqttClient.mqttClient().get();
                    return new ConnectionState(cachedMqttClient.id(),
                            true,
                            iMqttClient.getState().isConnected(),
                            subscriptionsForMqttClient.exists(iMqttClient.getConfig().getClientIdentifier().get().toString(), onboardingResponse.getConnectionCriteria().getCommands()),
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
