package de.agrirouter.middleware.business.listener;

import com.dke.data.agrirouter.api.enums.Gateway;
import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.messaging.mqtt.SetSubscriptionService;
import com.dke.data.agrirouter.api.service.parameters.SetSubscriptionParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.SetSubscriptionServiceImpl;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.UpdateSubscriptionsForEndpointEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.common.SubscriptionParameterFactory;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.jpa.ApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for endpoint maintenance.
 */
@Slf4j
@Service
public class UpdateSubscriptionsForEndpointEventListener {

    private final EndpointService endpointService;
    private final ApplicationRepository applicationRepository;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final MqttClientManagementService mqttClientManagementService;
    private final BusinessOperationLogService businessOperationLogService;
    private final SubscriptionParameterFactory subscriptionParameterFactory;

    public UpdateSubscriptionsForEndpointEventListener(EndpointService endpointService,
                                                       ApplicationRepository applicationRepository,
                                                       MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                                       MqttClientManagementService mqttClientManagementService,
                                                       BusinessOperationLogService businessOperationLogService,
                                                       SubscriptionParameterFactory subscriptionParameterFactory) {
        this.endpointService = endpointService;
        this.applicationRepository = applicationRepository;
        this.mqttClientManagementService = mqttClientManagementService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.businessOperationLogService = businessOperationLogService;
        this.subscriptionParameterFactory = subscriptionParameterFactory;
    }

    /**
     * Handle the event for the update of the subscriptions after the capabilities were received and the AR did send an ACK.
     *
     * @param updateSubscriptionsForEndpointEvent -
     */
    @Transactional
    @EventListener
    public void updateSubscriptionsForEndpoint(UpdateSubscriptionsForEndpointEvent updateSubscriptionsForEndpointEvent) {
        log.debug("Update subscriptions.");
        try {
            final var endpoint = endpointService.findByAgrirouterEndpointId(updateSubscriptionsForEndpointEvent.getAgrirouterEndpointId());
            resendSubscriptions(endpoint.getExternalEndpointId());
            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Subscriptions updated.");
        } catch (BusinessException e) {
            log.error(e.getErrorMessage().asLogMessage());
        }
    }

    /**
     * Method for resending the subscriptions.
     *
     * @param externalEndpointId The ID of the endpoint.
     */
    private void resendSubscriptions(String externalEndpointId) {
        final var optionalEndpoint = endpointService.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            var endpoint = optionalEndpoint.get();
            final var optionalApplication = applicationRepository.findByEndpointsContains(endpoint);
            if (optionalApplication.isPresent()) {
                final var application = optionalApplication.get();
                sendSubscriptions(application, endpoint);
            } else {
                log.error(ErrorMessageFactory.couldNotFindApplication().asLogMessage());
            }
        } else {
            log.warn("The endpoint with the external endpoint ID '{}' could not be found.", externalEndpointId);
        }
    }

    /**
     * Send capabilities and update subscriptions.
     *
     * @param application The application.
     * @param endpoint    The endpoint.
     */
    private void sendSubscriptions(Application application, Endpoint endpoint) {
        log.debug("Update the subscriptions for the endpoint with the id '{}'.", endpoint.getAgrirouterEndpointId());
        final var onboardingResponse = endpoint.asOnboardingResponse();
        if (Gateway.MQTT.getKey().equals(onboardingResponse.getConnectionCriteria().getGatewayId())) {
            log.debug("Handling MQTT onboard response updates.");
            final var subscriptions = subscriptionParameterFactory.create(application);
            enableSubscriptions(endpoint, subscriptions);
            log.debug("The following subscriptions [{}] for the endpoint with the id '{}' are send.", subscriptions
                    .stream()
                    .filter(subscription -> null != subscription.getTechnicalMessageType())
                    .map(subscription -> String.format("{%s,(%s)}", Objects.requireNonNull(subscription.getTechnicalMessageType()).getKey(), subscription.getDdis()
                            .stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(","))))
                    .collect(Collectors.joining(",")), endpoint.getExternalEndpointId());
        } else {
            log.error(ErrorMessageFactory.middlewareDoesNotSupportGateway(onboardingResponse.getConnectionCriteria().getGatewayId()).asLogMessage());
        }
    }

    /**
     * Enabling the subscriptions for the onboard response using the onboard response.
     *
     * @param endpoint      The endpoint.
     * @param subscriptions The subscriptions.
     */
    private void enableSubscriptions(Endpoint endpoint, List<SetSubscriptionParameters.Subscription> subscriptions) {
        log.debug("Enable the subscriptions for the endpoint with the id '{}'.", endpoint.getExternalEndpointId());
        final var iMqttClient = mqttClientManagementService.get(endpoint);
        if (iMqttClient.isEmpty() || !iMqttClient.get().isConnected()) {
            log.error("No MQTT client found for endpoint with the external endpoint ID '{}'.", endpoint.getExternalEndpointId());
        } else {
            SetSubscriptionParameters parameters = new SetSubscriptionParameters();
            parameters.setOnboardingResponse(endpoint.asOnboardingResponse());
            parameters.setSubscriptions(subscriptions);
            SetSubscriptionService setSubscriptionService = new SetSubscriptionServiceImpl(iMqttClient.get());
            final var messageId = setSubscriptionService.send(parameters);

            log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
            MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
            messageWaitingForAcknowledgement.setAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
            messageWaitingForAcknowledgement.setMessageId(messageId);
            messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_SUBSCRIPTION.getKey());
            messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
        }
    }

}
