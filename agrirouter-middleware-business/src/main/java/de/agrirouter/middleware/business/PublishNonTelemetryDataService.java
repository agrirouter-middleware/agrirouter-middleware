package de.agrirouter.middleware.business;

import com.google.protobuf.ByteString;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;
import de.agrirouter.middleware.integration.SendMessageIntegrationService;
import de.agrirouter.middleware.integration.parameters.MessagingIntegrationParameters;
import lombok.extern.slf4j.Slf4j;
import org.bson.internal.Base64;
import org.springframework.stereotype.Service;

import static de.agrirouter.middleware.api.logging.BusinessOperationLogService.NA;

/**
 * Service to handle business operations round about publishing non telemetry data.
 */
@Slf4j
@Service
public class PublishNonTelemetryDataService {

    private final SendMessageIntegrationService sendMessageIntegrationService;
    private final BusinessOperationLogService businessOperationLogService;

    public PublishNonTelemetryDataService(SendMessageIntegrationService sendMessageIntegrationService,
                                          BusinessOperationLogService businessOperationLogService) {
        this.sendMessageIntegrationService = sendMessageIntegrationService;
        this.businessOperationLogService = businessOperationLogService;
    }

    /**
     * Publish message content.
     *
     * @param publishNonTelemetryDataParameters -
     */
    public void publish(PublishNonTelemetryDataParameters publishNonTelemetryDataParameters) {
        final var messagingIntegrationParameters = new MessagingIntegrationParameters();
        messagingIntegrationParameters.setExternalEndpointId(publishNonTelemetryDataParameters.getExternalEndpointId());
        messagingIntegrationParameters.setTechnicalMessageType(publishNonTelemetryDataParameters.getContentMessageType());
        messagingIntegrationParameters.setMessage(asByteString(publishNonTelemetryDataParameters.getBase64EncodedMessageContent()));
        messagingIntegrationParameters.setFilename(publishNonTelemetryDataParameters.getFilename());
        messagingIntegrationParameters.setRecipients(publishNonTelemetryDataParameters.getRecipients());
        sendMessageIntegrationService.publish(messagingIntegrationParameters);
        businessOperationLogService.log(new EndpointLogInformation(publishNonTelemetryDataParameters.getExternalEndpointId(), NA), "Non telemetry data published");
    }

    private ByteString asByteString(String base64EncodedMessageContent) {
        return ByteString.copyFrom(Base64.decode(base64EncodedMessageContent));
    }

}
