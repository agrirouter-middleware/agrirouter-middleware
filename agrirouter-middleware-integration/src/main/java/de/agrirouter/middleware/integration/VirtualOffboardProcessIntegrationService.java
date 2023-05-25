package de.agrirouter.middleware.integration;

import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.parameters.CloudOffboardingParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.CloudOffboardingServiceImpl;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.integration.ack.DynamicMessageProperties;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.parameters.VirtualOffboardProcessIntegrationParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Integration service to handle the virtual onboard requests.
 */
@Slf4j
@Service
public class VirtualOffboardProcessIntegrationService {

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
        final var iMqttClient = mqttClientManagementService.get(virtualOffboardProcessIntegrationParameters.parentEndpoint());
        if (iMqttClient.isEmpty()) {
            throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(virtualOffboardProcessIntegrationParameters.parentEndpoint().getAgrirouterEndpointId()));
        }

        var onboardingResponse = virtualOffboardProcessIntegrationParameters.parentEndpoint().asOnboardingResponse();
        final var cloudOffboardingService = new CloudOffboardingServiceImpl(iMqttClient.get());
        final var parameters = new CloudOffboardingParameters();
        parameters.setOnboardingResponse(onboardingResponse);
        parameters.setEndpointIds(virtualOffboardProcessIntegrationParameters.virtualEndpointIds());
        final var messageId = cloudOffboardingService.send(parameters);

        log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
        MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
        messageWaitingForAcknowledgement.setAgrirouterEndpointId(virtualOffboardProcessIntegrationParameters.parentEndpoint().getAgrirouterEndpointId());
        messageWaitingForAcknowledgement.setMessageId(messageId);
        messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_CLOUD_OFFBOARD_ENDPOINTS.getKey());
        final var dynamicProperties = new HashMap<String, Object>();
        dynamicProperties.put(DynamicMessageProperties.EXTERNAL_VIRTUAL_ENDPOINT_IDS, virtualOffboardProcessIntegrationParameters.virtualEndpointIds());
        messageWaitingForAcknowledgement.setDynamicProperties(dynamicProperties);
        messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
    }

}
