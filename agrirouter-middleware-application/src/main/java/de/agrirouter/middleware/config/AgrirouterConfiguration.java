package de.agrirouter.middleware.config;

import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.env.Production;
import com.dke.data.agrirouter.api.env.QA;
import com.dke.data.agrirouter.api.service.RevokingService;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodePushNotificationService;
import com.dke.data.agrirouter.api.service.messaging.encoding.EncodeMessageService;
import com.dke.data.agrirouter.api.service.onboard.OnboardingService;
import com.dke.data.agrirouter.api.service.onboard.secured.AuthorizationRequestService;
import com.dke.data.agrirouter.convenience.decode.DecodeCloudOnboardingResponsesService;
import com.dke.data.agrirouter.convenience.mqtt.client.MqttClientService;
import com.dke.data.agrirouter.convenience.mqtt.client.MqttOptionService;
import com.dke.data.agrirouter.impl.messaging.encoding.DecodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.encoding.DecodePushNotificationServiceImpl;
import com.dke.data.agrirouter.impl.messaging.encoding.EncodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.onboard.OnboardingServiceImpl;
import com.dke.data.agrirouter.impl.onboard.secured.AuthorizationRequestServiceImpl;
import com.dke.data.agrirouter.impl.revoke.RevokingServiceImpl;
import de.agrirouter.middleware.integration.mqtt.MessageHandlingCallback;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * The internal configuration for the agrirouter© connection.
 */
@Configuration
public class AgrirouterConfiguration {

    /**
     * Create an instance of the QA environment for dependency injection.
     *
     * @return -
     */
    @Bean
    @Profile("!connect-agrirouter-prod")
    public Environment qa() {
        return new QA() {
        };
    }

    /**
     * Create an instance of the QA environment for dependency injection.
     *
     * @return -
     */
    @Bean
    @Profile("connect-agrirouter-prod")
    public Environment prod() {
        return new Production() {
        };
    }

    /**
     * Create an instance of the decoded message service used for message decoding.
     *
     * @return -
     */
    @Bean
    public DecodeMessageService decodeMessageService() {
        return new DecodeMessageServiceImpl();
    }

    /**
     * Create an instance of the service to create MQTT clients.
     *
     * @param environment The current environment, injected.
     * @return -
     */
    @Bean
    public MqttClientService mqqMqttClientService(Environment environment) {
        return new MqttClientService(environment);
    }

    /**
     * Create an instance of the service for MQTT options.
     *
     * @param environment The current environment, injected.
     * @return -
     */
    @Bean
    public MqttOptionService mqttOptionService(Environment environment) {
        return new MqttOptionService(environment);
    }

    /**
     * Create an instance of the service to onboard endpoints.
     *
     * @param environment The current environment, injected.
     * @return -
     */
    @Bean
    public OnboardingService onboardingService(Environment environment) {
        return new OnboardingServiceImpl(environment);
    }

    /**
     * Create an instance of the service to onboard endpoints.
     *
     * @param environment The current environment, injected.
     * @return -
     */
    @Bean
    public com.dke.data.agrirouter.api.service.onboard.secured.OnboardingService securedOnboardingService(Environment environment) {
        return new com.dke.data.agrirouter.impl.onboard.secured.OnboardingServiceImpl(environment);
    }

    /**
     * Create an instance of the service to revoke endpoints.
     *
     * @param environment The current environment, injected.
     * @return -
     */
    @Bean
    public RevokingService revokingService(Environment environment) {
        return new RevokingServiceImpl(environment);
    }

    /**
     * Create an instance of the message handler.
     *
     * @return -
     */
    @Bean
    public MessageHandlingCallback messageHandlingCallback(ApplicationEventPublisher applicationEventPublisher,
                                                           DecodeMessageService decodeMessageService) {
        return new MessageHandlingCallback(applicationEventPublisher, decodeMessageService);
    }

    /**
     * Create an instance of the service to decode push notifications.
     *
     * @return -
     */
    @Bean
    public DecodePushNotificationService decodePushNotificationService() {
        return new DecodePushNotificationServiceImpl();
    }

    /**
     * Create an instance of the service to generate the authorization request.
     *
     * @return -
     */
    @Bean
    public AuthorizationRequestService authorizationRequestService(Environment environment) {
        return new AuthorizationRequestServiceImpl(environment);
    }

    /**
     * Create an instance of the service to encode messages.
     *
     * @return -
     */
    @Bean
    public EncodeMessageService encodeMessageService() {
        return new EncodeMessageServiceImpl();
    }


    /**
     * Create an instance of the service to decode cloud onboard responses.
     *
     * @return -
     */
    @Bean
    public DecodeCloudOnboardingResponsesService decodeCloudOnboardingResponsesService() {
        return new DecodeCloudOnboardingResponsesService();
    }
}
