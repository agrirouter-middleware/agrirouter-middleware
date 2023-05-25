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
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.agrirouter.middleware.integration.ack.DynamicMessageProperties.EXTERNAL_VIRTUAL_ENDPOINT_ID;

/**
 * Integration service to handle the virtual onboard requests.
 */
@Slf4j
@Service
public class VirtualOnboardProcessIntegrationService {

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
    @Async
    public void onboard(VirtualOnboardProcessIntegrationParameters virtualOnboardProcessIntegrationParameters) {
        final var iMqttClient = mqttClientManagementService.get(virtualOnboardProcessIntegrationParameters.parentEndpoint());
        if (iMqttClient.isEmpty()) {
            throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(virtualOnboardProcessIntegrationParameters.parentEndpoint().getAgrirouterEndpointId()));
        }
        final var onboardingResponse = virtualOnboardProcessIntegrationParameters.parentEndpoint().asOnboardingResponse();
        final var cloudOnboardingService = new CloudOnboardingServiceImpl(iMqttClient.get());
        final var parameters = new CloudOnboardingParameters();
        parameters.setOnboardingResponse(onboardingResponse);

        List<CloudOnboardingParameters.EndpointDetailsParameters> endpointDetails = new ArrayList<>();

        final var endpointDetailsParameters = new CloudOnboardingParameters.EndpointDetailsParameters();
        endpointDetailsParameters.setEndpointId(virtualOnboardProcessIntegrationParameters.externalVirtualEndpointId());
        endpointDetailsParameters.setEndpointName(virtualOnboardProcessIntegrationParameters.endpointName());
        endpointDetails.add(endpointDetailsParameters);
        parameters.setEndpointDetails(endpointDetails);

        final var messageId = cloudOnboardingService.send(parameters);
        log.debug("Pushing the message ID '{}' to the stack to fetch the endpoint ID afterwards.", messageId);
        virtualEndpointOnboardStateContainer.push(messageId, endpointDetailsParameters.getEndpointId());

        log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
        MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
        messageWaitingForAcknowledgement.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
        messageWaitingForAcknowledgement.setMessageId(messageId);
        messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_CLOUD_ONBOARD_ENDPOINTS.getKey());
        final var dynamicProperties = new HashMap<String, Object>();
        dynamicProperties.put(EXTERNAL_VIRTUAL_ENDPOINT_ID, endpointDetailsParameters.getEndpointId());
        messageWaitingForAcknowledgement.setDynamicProperties(dynamicProperties);
        messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
    }
}
