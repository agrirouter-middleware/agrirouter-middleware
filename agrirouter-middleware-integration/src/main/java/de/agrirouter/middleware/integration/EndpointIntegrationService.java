package de.agrirouter.middleware.integration;

import agrirouter.request.payload.endpoint.Capabilities;
import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.messaging.mqtt.SetCapabilityService;
import com.dke.data.agrirouter.api.service.parameters.SetCapabilitiesParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.SetCapabilityServiceImpl;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.common.CapabilityParameterFactory;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for endpoint maintenance.
 */
@Slf4j
@Service
public class EndpointIntegrationService {

    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final MqttClientManagementService mqttClientManagementService;

    public EndpointIntegrationService(MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                      MqttClientManagementService mqttClientManagementService) {
        this.mqttClientManagementService = mqttClientManagementService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
    }

    /**
     * Send capabilities.
     *
     * @param application The application.
     * @param endpoint    The endpoint.
     */
    public void sendCapabilities(Application application, Endpoint endpoint) {
        enableCapabilities(application, endpoint, CapabilityParameterFactory.create(application));
    }

    /**
     * Enabling the capabilities for the onboard response using the given application.
     *
     * @param application  The application, needed to define application ID and certification ID.
     * @param endpoint     The endpoint.
     * @param capabilities The capabilities.
     */
    public void enableCapabilities(Application application, Endpoint endpoint, List<SetCapabilitiesParameters.CapabilityParameters> capabilities) {
        final var iMqttClient = mqttClientManagementService.get(endpoint);
        if (iMqttClient.isEmpty()) {
            throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.getAgrirouterEndpointId()));
        }
        SetCapabilitiesParameters parameters = new SetCapabilitiesParameters();
        var onboardingResponse = endpoint.asOnboardingResponse();
        parameters.setOnboardingResponse(onboardingResponse);
        parameters.setApplicationId(application.getApplicationId());
        parameters.setCertificationVersionId(application.getVersionId());
        parameters.setEnablePushNotifications(Capabilities.CapabilitySpecification.PushNotification.ENABLED);
        parameters.setCapabilitiesParameters(capabilities);

        SetCapabilityService setCapabilityService = new SetCapabilityServiceImpl(iMqttClient.get());
        final var messageId = setCapabilityService.send(parameters);

        log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
        MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
        messageWaitingForAcknowledgement.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
        messageWaitingForAcknowledgement.setMessageId(messageId);
        messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_CAPABILITIES.getKey());
        messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
    }

}
