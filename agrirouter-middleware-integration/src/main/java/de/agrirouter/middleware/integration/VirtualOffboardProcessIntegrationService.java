package de.agrirouter.middleware.integration;

import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.parameters.CloudOffboardingParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.CloudOffboardingServiceImpl;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.parameters.VirtualOffboardProcessIntegrationParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Integration service to handle the virtual onboard requests.
 */
@Service
public class VirtualOffboardProcessIntegrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualOffboardProcessIntegrationService.class);

    private final MqttClientManagementService mqttClientManagementService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;

    public VirtualOffboardProcessIntegrationService(MqttClientManagementService mqttClientManagementService,
                                                    MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService) {
        this.mqttClientManagementService = mqttClientManagementService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
    }

    /**
     * Virtual endpoint offboard process.
     *
     * @param virtualOffboardProcessIntegrationParameters The parameters for the offboard process.
     */
    public void offboard(VirtualOffboardProcessIntegrationParameters virtualOffboardProcessIntegrationParameters) {
        final var onboardingResponse = virtualOffboardProcessIntegrationParameters.getEndpoint().asOnboardingResponse();
        final var iMqttClient = mqttClientManagementService.get(onboardingResponse);
        if (iMqttClient.isEmpty()) {
            throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(onboardingResponse.getSensorAlternateId()));
        }
        final var cloudOffboardingService = new CloudOffboardingServiceImpl(iMqttClient.get());
        final var parameters = new CloudOffboardingParameters();
        parameters.setOnboardingResponse(onboardingResponse);
        parameters.setEndpointIds(virtualOffboardProcessIntegrationParameters.getEndpointIds());
        final var messageId = cloudOffboardingService.send(parameters);

        LOGGER.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
        MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
        messageWaitingForAcknowledgement.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
        messageWaitingForAcknowledgement.setMessageId(messageId);
        messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_CLOUD_OFFBOARD_ENDPOINTS.getKey());
        messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
    }

}
