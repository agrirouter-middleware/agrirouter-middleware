package de.agrirouter.middleware.integration.mqtt.health.internal;

import agrirouter.commons.MessageOuterClass;
import agrirouter.request.Request;
import com.dke.data.agrirouter.api.dto.encoding.EncodedMessage;
import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.exception.CouldNotSendMqttMessageException;
import com.dke.data.agrirouter.api.service.messaging.encoding.EncodeMessageService;
import com.dke.data.agrirouter.api.service.parameters.MessageHeaderParameters;
import com.dke.data.agrirouter.api.service.parameters.PayloadParameters;
import com.dke.data.agrirouter.api.service.parameters.SendMessageParameters;
import com.dke.data.agrirouter.impl.common.MessageIdService;
import com.dke.data.agrirouter.impl.messaging.MessageBodyCreator;
import com.dke.data.agrirouter.impl.messaging.SequenceNumberService;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;

/**
 * Service implementation.
 *
 * @deprecated This class is deprecated since version 9.0.0 and will be removed soon.
 * It is a redundant implementation of the service in the SDK,
 * but since the SDK relies on Jakarta, we cannot use it yet.
 */
@Service
@Deprecated(since = "9.0.0", forRemoval = true)
@RequiredArgsConstructor
public class PingService implements MessageBodyCreator {

    private final EncodeMessageService encodeMessageService;

    public String send(IMqttClient mqttClient, OnboardingResponse onboardingResponse) {
        try {
            var encodedMessage = this.encode(onboardingResponse);
            var sendMessageParameters = new SendMessageParameters();
            sendMessageParameters.setOnboardingResponse(onboardingResponse);
            sendMessageParameters.setEncodedMessages(
                    Collections.singletonList(encodedMessage.getEncodedMessage()));
            var messageAsJson = this.createMessageBody(sendMessageParameters);
            var payload = messageAsJson.getBytes();
            mqttClient.publish(
                    Objects.requireNonNull(onboardingResponse)
                            .getConnectionCriteria()
                            .getMeasures(),
                    new MqttMessage(payload));
            return encodedMessage.getApplicationMessageID();
        } catch (MqttException e) {
            throw new CouldNotSendMqttMessageException(e);
        }
    }

    private EncodedMessage encode(OnboardingResponse onboardingResponse) {
        final var applicationMessageID = MessageIdService.generateMessageId();

        var messageContent = ""; // No content for ping messages.

        var messageHeaderParameters = new MessageHeaderParameters();
        messageHeaderParameters.setApplicationMessageId(applicationMessageID);
        messageHeaderParameters.setTechnicalMessageType(SystemMessageType.DKE_PING);
        messageHeaderParameters.setMode(Request.RequestEnvelope.Mode.DIRECT);
        messageHeaderParameters.setMetadata(MessageOuterClass.Metadata.newBuilder().build());

        setSequenceNumber(messageHeaderParameters, onboardingResponse);
        var payloadParameters = new PayloadParameters();
        payloadParameters.setTypeUrl(SystemMessageType.DKE_PING.getTypeUrl());

        payloadParameters.setValue(ByteString.copyFrom(messageContent.getBytes()));

        var encodedMessage = encodeMessageService.encode(messageHeaderParameters, payloadParameters);
        return new EncodedMessage(applicationMessageID, encodedMessage);
    }

    private void setSequenceNumber(MessageHeaderParameters messageHeaderParameters, OnboardingResponse onboardingResponse) {
        messageHeaderParameters.setApplicationMessageSeqNo(SequenceNumberService.generateSequenceNumberForEndpoint(onboardingResponse));
    }
}