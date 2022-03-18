package de.agrirouter.middleware.integration;

import agrirouter.commons.MessageOuterClass;
import agrirouter.request.Request;
import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.service.messaging.encoding.EncodeMessageService;
import com.dke.data.agrirouter.api.service.parameters.MessageHeaderParameters;
import com.dke.data.agrirouter.api.service.parameters.PayloadParameters;
import com.dke.data.agrirouter.api.service.parameters.SendMessageParameters;
import com.dke.data.agrirouter.impl.common.MessageIdService;
import com.dke.data.agrirouter.impl.messaging.SequenceNumberService;
import com.dke.data.agrirouter.impl.messaging.mqtt.SendMessageServiceImpl;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.businesslog.BusinessLogService;
import de.agrirouter.middleware.integration.ack.DynamicMessageProperties;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.parameters.MessagingIntegrationParameters;
import de.agrirouter.middleware.persistence.EndpointRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Messaging service for sending or publishing messages.
 */
@Service
public class SendMessageIntegrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageIntegrationService.class);

    private final MqttClientManagementService mqttClientManagementService;
    private final EncodeMessageService encodeMessageService;
    private final BusinessLogService businessLogService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final EndpointRepository endpointRepository;

    public SendMessageIntegrationService(MqttClientManagementService mqttClientManagementService,
                                         EncodeMessageService encodeMessageService,
                                         BusinessLogService businessLogService,
                                         MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                         EndpointRepository endpointRepository) {
        this.mqttClientManagementService = mqttClientManagementService;
        this.encodeMessageService = encodeMessageService;
        this.businessLogService = businessLogService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.endpointRepository = endpointRepository;
    }

    /**
     * Publish a message. If the recipients are set also, the sending mode will be only direct sending, you have to decice between publishing and direct sending.
     *
     * @param messagingIntegrationParameters -
     */
    public void publish(MessagingIntegrationParameters messagingIntegrationParameters) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDisabled(messagingIntegrationParameters.getExternalEndpointId());
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            final var onboardingResponse = endpoint.asOnboardingResponse();
            final var messageHeaderParameters = createMessageHeaderParameters(messagingIntegrationParameters, onboardingResponse);
            final var payloadParameters = createPayloadParameters(messagingIntegrationParameters);

            final var messageParameterTuples = encodeMessageService.chunkAndEncode(messageHeaderParameters, payloadParameters, onboardingResponse);
            final var encodedMessages = encodeMessageService.encode(messageParameterTuples);

            final var iMqttClient = mqttClientManagementService.get(onboardingResponse);
            if (iMqttClient.isEmpty()) {
                throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(onboardingResponse.getSensorAlternateId()));
            }
            SendMessageServiceImpl sendMessageService = new SendMessageServiceImpl(iMqttClient.get());
            SendMessageParameters sendMessageParameters = new SendMessageParameters();
            sendMessageParameters.setOnboardingResponse(onboardingResponse);
            sendMessageParameters.setEncodedMessages(encodedMessages);
            sendMessageParameters.setTeamsetContextId(messagingIntegrationParameters.getTeamSetContextId());
            sendMessageService.send(sendMessageParameters);
            businessLogService.publishMessage(endpoint, messagingIntegrationParameters.getTechnicalMessageType());

            LOGGER.debug("Saving message with ID '{}'  waiting for ACK.", messageHeaderParameters.getApplicationMessageId());
            MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
            messageWaitingForAcknowledgement.setAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
            messageWaitingForAcknowledgement.setMessageId(messageHeaderParameters.getApplicationMessageId());
            messageWaitingForAcknowledgement.setTechnicalMessageType(messagingIntegrationParameters.getTechnicalMessageType().getKey());
            messageWaitingForAcknowledgement.getDynamicProperties().put(DynamicMessageProperties.TEAM_SET_CONTEXT_ID, messagingIntegrationParameters.getTeamSetContextId());
            messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }

    private PayloadParameters createPayloadParameters(MessagingIntegrationParameters messagingIntegrationParameters) {
        final var payloadParameters = new PayloadParameters();
        payloadParameters.setTypeUrl(messagingIntegrationParameters.getTechnicalMessageType().getTypeUrl());
        payloadParameters.setValue(messagingIntegrationParameters.getMessage());
        return payloadParameters;
    }

    private MessageHeaderParameters createMessageHeaderParameters(MessagingIntegrationParameters messagingIntegrationParameters, OnboardingResponse onboardingResponse) {
        final var messageHeaderParameters = new MessageHeaderParameters();
        messageHeaderParameters.setApplicationMessageId(MessageIdService.generateMessageId());
        messageHeaderParameters.setApplicationMessageSeqNo(SequenceNumberService.generateSequenceNumberForEndpoint(onboardingResponse));
        messageHeaderParameters.setTechnicalMessageType(messagingIntegrationParameters.getTechnicalMessageType());
        final var metadataBuilder = MessageOuterClass.Metadata.newBuilder();
        if (StringUtils.isNotBlank(messagingIntegrationParameters.getFilename())) {
            metadataBuilder.setFileName(messagingIntegrationParameters.getFilename());
        }
        messageHeaderParameters.setMetadata(metadataBuilder.build());

        if (null != messagingIntegrationParameters.getRecipients() && !messagingIntegrationParameters.getRecipients().isEmpty()) {
            messageHeaderParameters.setRecipients(new ArrayList<>(messagingIntegrationParameters.getRecipients()));
            messageHeaderParameters.setMode(Request.RequestEnvelope.Mode.DIRECT);
        } else {
            messageHeaderParameters.setMode(Request.RequestEnvelope.Mode.PUBLISH);
        }

        if (StringUtils.isNotBlank(messagingIntegrationParameters.getTeamSetContextId())) {
            messageHeaderParameters.setTeamSetContextId(messagingIntegrationParameters.getTeamSetContextId());
        }

        // FIXME Add chunking.
        return messageHeaderParameters;
    }

}
