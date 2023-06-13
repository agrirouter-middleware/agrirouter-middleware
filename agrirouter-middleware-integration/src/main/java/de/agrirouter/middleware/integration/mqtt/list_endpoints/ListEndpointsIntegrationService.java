package de.agrirouter.middleware.integration.mqtt.list_endpoints;

import agrirouter.request.payload.account.Endpoints;
import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.parameters.ListEndpointsParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.ListEndpointsServiceImpl;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

/**
 * Service to list all endpoints for a specific endpoint within the middleware.
 */
@Slf4j
@Service
public class ListEndpointsIntegrationService {

    private final MqttClientManagementService mqttClientManagementService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final ListEndpointsMessages listEndpointsMessages;

    public ListEndpointsIntegrationService(MqttClientManagementService mqttClientManagementService,
                                           MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                           ListEndpointsMessages listEndpointsMessages) {
        this.mqttClientManagementService = mqttClientManagementService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.listEndpointsMessages = listEndpointsMessages;
    }

    /**
     * Publish a list endpoints message for the given onboarding response.
     *
     * @param endpoint The endpoint.
     */
    public void publishListEndpointsMessage(Endpoint endpoint) {
        var iMqttClient = mqttClientManagementService.get(endpoint);
        if (iMqttClient.isPresent()) {
            final var listEndpointsService = new ListEndpointsServiceImpl(iMqttClient.get());
            final var parameters = new ListEndpointsParameters();
            parameters.setOnboardingResponse(endpoint.asOnboardingResponse());
            parameters.setDirection(Endpoints.ListEndpointsQuery.Direction.SEND);
            parameters.setTechnicalMessageType(SystemMessageType.EMPTY);
            parameters.setUnfilteredList(false);
            final var messageId = listEndpointsService.send(parameters);

            var listEndpointsMessage = new ListEndpointsMessage();
            listEndpointsMessage.setTimestamp(Instant.now().toEpochMilli());
            listEndpointsMessage.setAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
            listEndpointsMessages.put(listEndpointsMessage);

            log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
            MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
            messageWaitingForAcknowledgement.setAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
            messageWaitingForAcknowledgement.setMessageId(messageId);
            messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_LIST_ENDPOINTS.getKey());
            messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
        } else {
            log.warn("Could not find MQTT client for endpoint with the external endpoint ID '{}'.", endpoint.getExternalEndpointId());
        }

    }

    /**
     * Check if there is a pending list endpoints response for the given endpoint ID.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @return True if there is a pending list endpoints response, false otherwise.
     */
    public boolean hasPendingResponse(String agrirouterEndpointId) {
        var listEndpointsMessage = listEndpointsMessages.get(agrirouterEndpointId);
        return listEndpointsMessage != null;
    }

    /**
     * Get all message recipients from the incoming response of the agrirouter©.
     *
     * @param agrirouterEndpointId The endpoint ID.
     * @return A collection of message recipients.
     */
    public Optional<Collection<MessageRecipient>> getRecipients(String agrirouterEndpointId) {
        var listEndpointsMessage = listEndpointsMessages.get(agrirouterEndpointId);
        if (listEndpointsMessage == null) {
            log.warn("No list endpoints message found for endpoint ID {}.", agrirouterEndpointId);
            return Optional.empty();
        }
        var hasBeenReturned = listEndpointsMessage.isHasBeenReturned();
        if (!hasBeenReturned) {
            log.debug("List endpoints message for endpoint ID {} has not been returned.", agrirouterEndpointId);
        } else {
            log.info("List endpoints message for endpoint ID {} has been returned.", agrirouterEndpointId);
            listEndpointsMessages.remove(agrirouterEndpointId);
        }
        return Optional.ofNullable(listEndpointsMessage.getMessageRecipients());
    }


}
