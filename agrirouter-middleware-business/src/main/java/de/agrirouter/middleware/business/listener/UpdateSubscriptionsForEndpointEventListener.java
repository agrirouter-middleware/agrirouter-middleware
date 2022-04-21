package de.agrirouter.middleware.business.listener;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
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
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.common.SubscriptionParameterFactory;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.ApplicationRepository;
import de.agrirouter.middleware.persistence.EndpointRepository;
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

    private final EndpointRepository endpointRepository;
    private final ApplicationRepository applicationRepository;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final MqttClientManagementService mqttClientManagementService;
    private final BusinessOperationLogService businessOperationLogService;

    public UpdateSubscriptionsForEndpointEventListener(EndpointRepository endpointRepository,
                                                       ApplicationRepository applicationRepository,
                                                       MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                                       MqttClientManagementService mqttClientManagementService,
                                                       BusinessOperationLogService businessOperationLogService) {
        this.endpointRepository = endpointRepository;
        this.applicationRepository = applicationRepository;
        this.mqttClientManagementService = mqttClientManagementService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.businessOperationLogService = businessOperationLogService;
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
        final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(updateSubscriptionsForEndpointEvent.getAgrirouterEndpointId());
        if (optionalEndpoint.isPresent()) {
            resendSubscriptions(optionalEndpoint.get().getExternalEndpointId());
            businessOperationLogService.log(new EndpointLogInformation(optionalEndpoint.get().getExternalEndpointId(), optionalEndpoint.get().getAgrirouterEndpointId()), "Subscriptions updated.");
        } else {
            log.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
        }
    }

    /**
     * Method for resending the subscriptions.
     *
     * @param externalEndpointId The ID of the endpoint.
     */
    private void resendSubscriptions(String externalEndpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDisabled(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            final var optionalApplication = applicationRepository.findByEndpointsContains(endpoint);
            if (optionalApplication.isPresent()) {
                final var application = optionalApplication.get();
                sendSubscriptions(application, endpoint);
            } else {
                log.error(ErrorMessageFactory.couldNotFindApplication().asLogMessage());
            }
        } else {
            log.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
        }
    }

    /**
     * Send capabilities and update subscriptions.
     *
     * @param application The application.
     * @param endpoint    The endpoint.
     */
    private void sendSubscriptions(Application application, Endpoint endpoint) {
        log.debug("Update the subscriptions for the endpoint with the id '{}'.", endpoint.getId());
        final var onboardingResponse = endpoint.asOnboardingResponse();
        if (Gateway.MQTT.getKey().equals(onboardingResponse.getConnectionCriteria().getGatewayId())) {
            log.debug("Handling MQTT onboard response updates.");
            final var subscriptions = SubscriptionParameterFactory.create(application);
            enableSubscriptions(onboardingResponse, subscriptions);
            log.debug(String.format("The following subscriptions [%s] for the endpoint with the id '%s' are send.", subscriptions
                    .stream()
                    .filter(subscription -> null != subscription.getTechnicalMessageType())
                    .map(subscription -> String.format("{%s,(%s)}", Objects.requireNonNull(subscription.getTechnicalMessageType()).getKey(), subscription.getDdis()
                            .stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(","))))
                    .collect(Collectors.joining(",")), endpoint.getExternalEndpointId()));
        } else {
            log.error(ErrorMessageFactory.middlewareDoesNotSupportGateway(onboardingResponse.getConnectionCriteria().getGatewayId()).asLogMessage());
        }
    }

    /**
     * Enabling the subscriptions for the onboard response using the onboard response.
     *
     * @param onboardingResponse The onboard response.
     * @param subscriptions      The subscriptions.
     */
    private void enableSubscriptions(OnboardingResponse onboardingResponse, List<SetSubscriptionParameters.Subscription> subscriptions) {
        log.debug("Enable the subscriptions for the endpoint with the id '{}'.", onboardingResponse.getSensorAlternateId());
        SetSubscriptionParameters parameters = new SetSubscriptionParameters();
        parameters.setOnboardingResponse(onboardingResponse);
        parameters.setSubscriptions(subscriptions);
        final var iMqttClient = mqttClientManagementService.get(onboardingResponse);
        if (iMqttClient.isEmpty()) {
            throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(onboardingResponse.getSensorAlternateId()));
        }
        SetSubscriptionService setSubscriptionService = new SetSubscriptionServiceImpl(iMqttClient.get());
        final var messageId = setSubscriptionService.send(parameters);

        log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
        MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
        messageWaitingForAcknowledgement.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
        messageWaitingForAcknowledgement.setMessageId(messageId);
        messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_SUBSCRIPTION.getKey());
        messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
    }

}
