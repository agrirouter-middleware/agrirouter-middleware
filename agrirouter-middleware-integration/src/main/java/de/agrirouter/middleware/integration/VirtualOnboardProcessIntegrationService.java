package de.agrirouter.middleware.integration;

import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.parameters.CloudOnboardingParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.CloudOnboardingServiceImpl;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.container.VirtualEndpointOnboardStateContainer;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.parameters.VirtualOnboardProcessIntegrationParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Integration service to handle the virtual onboard requests.
 */
@Service
public class VirtualOnboardProcessIntegrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualOnboardProcessIntegrationService.class);

    private final MqttClientManagementService mqttClientManagementService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final VirtualEndpointOnboardStateContainer virtualEndpointOnboardStateContainer;

    public VirtualOnboardProcessIntegrationService(MqttClientManagementService mqttClientManagementService,
                                                   MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                                   VirtualEndpointOnboardStateContainer virtualEndpointOnboardStateContainer) {
        this.mqttClientManagementService = mqttClientManagementService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.virtualEndpointOnboardStateContainer = virtualEndpointOnboardStateContainer;
    }

    /**
     * Virtual endpoint onboard process.
     *
     * @param virtualOnboardProcessIntegrationParameters The parameters for the onboard process.
     */
    public void onboard(VirtualOnboardProcessIntegrationParameters virtualOnboardProcessIntegrationParameters) {
        final var onboardingResponse = virtualOnboardProcessIntegrationParameters.getEndpoint().asOnboardingResponse();
        final var iMqttClient = mqttClientManagementService.get(onboardingResponse);
        if (iMqttClient.isEmpty()) {
            throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(onboardingResponse.getSensorAlternateId()));
        }
        final var cloudOnboardingService = new CloudOnboardingServiceImpl(iMqttClient.get());
        final var parameters = new CloudOnboardingParameters();
        parameters.setOnboardingResponse(onboardingResponse);

        List<CloudOnboardingParameters.EndpointDetailsParameters> endpointDetails = new ArrayList<>();

        final var endpointDetailsParameters = new CloudOnboardingParameters.EndpointDetailsParameters();
        endpointDetailsParameters.setEndpointId(virtualOnboardProcessIntegrationParameters.getExternalVirtualEndpointId());
        endpointDetailsParameters.setEndpointName(virtualOnboardProcessIntegrationParameters.getEndpointName());
        endpointDetails.add(endpointDetailsParameters);
        parameters.setEndpointDetails(endpointDetails);

        final var messageId = cloudOnboardingService.send(parameters);
        LOGGER.debug("Pushing the message ID '{}' to the stack to fetch the endpoint ID afterwards.", messageId);
        virtualEndpointOnboardStateContainer.push(messageId, endpointDetailsParameters.getEndpointId());

        LOGGER.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
        MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
        messageWaitingForAcknowledgement.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
        messageWaitingForAcknowledgement.setMessageId(messageId);
        messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_CLOUD_ONBOARD_ENDPOINTS.getKey());
        messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
    }
}
