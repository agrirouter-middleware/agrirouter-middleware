package de.agrirouter.middleware.business;

import com.google.protobuf.ByteString;
import de.agrirouter.middleware.api.errorhandling.CriticalBusinessException;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.cache.messaging.MessageCache;
import de.agrirouter.middleware.business.events.ResendMessageCacheEntryEvent;
import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;
import de.agrirouter.middleware.integration.SendMessageIntegrationService;
import de.agrirouter.middleware.integration.parameters.MessagingIntegrationParameters;
import de.agrirouter.middleware.integration.status.AgrirouterStatusIntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Base64;

import static de.agrirouter.middleware.api.logging.BusinessOperationLogService.NA;

/**
 * Service to handle business operations round about publishing non telemetry data.
 */
@Slf4j
@Service
public class PublishNonTelemetryDataService {

    private final SendMessageIntegrationService sendMessageIntegrationService;
    private final BusinessOperationLogService businessOperationLogService;
    private final EndpointService endpointService;
    private final MessageCache messageCache;
    private final AgrirouterStatusIntegrationService agrirouterStatusIntegrationService;

    public PublishNonTelemetryDataService(SendMessageIntegrationService sendMessageIntegrationService,
                                          BusinessOperationLogService businessOperationLogService,
                                          EndpointService endpointService,
                                          MessageCache messageCache,
                                          AgrirouterStatusIntegrationService agrirouterStatusIntegrationService) {
        this.sendMessageIntegrationService = sendMessageIntegrationService;
        this.businessOperationLogService = businessOperationLogService;
        this.endpointService = endpointService;
        this.messageCache = messageCache;
        this.agrirouterStatusIntegrationService = agrirouterStatusIntegrationService;
    }

    /**
     * Publish message content.
     *
     * @param publishNonTelemetryDataParameters -
     */
    @EventListener(ResendMessageCacheEntryEvent.class)
    public void publish(PublishNonTelemetryDataParameters publishNonTelemetryDataParameters) {
        final var messagingIntegrationParameters = new MessagingIntegrationParameters(publishNonTelemetryDataParameters.getExternalEndpointId(),
                publishNonTelemetryDataParameters.getContentMessageType(),
                publishNonTelemetryDataParameters.getRecipients(),
                publishNonTelemetryDataParameters.getFilename(),
                asByteString(publishNonTelemetryDataParameters.getBase64EncodedMessageContent()),
                null);
        try {
            agrirouterStatusIntegrationService.checkCurrentStatus();
            if (checkConnectionForEndpoint(publishNonTelemetryDataParameters.getExternalEndpointId())) {
                sendMessageIntegrationService.publish(messagingIntegrationParameters);
                businessOperationLogService.log(new EndpointLogInformation(publishNonTelemetryDataParameters.getExternalEndpointId(), NA), "Non telemetry data published");
            } else {
                log.warn("Could not publish data. No connection to agrirouter©.");
                log.info("Endpoint ID: {}", publishNonTelemetryDataParameters.getExternalEndpointId());
                messageCache.put(publishNonTelemetryDataParameters.getExternalEndpointId(), messagingIntegrationParameters);
                businessOperationLogService.log(new EndpointLogInformation(publishNonTelemetryDataParameters.getExternalEndpointId(), NA), "Non telemetry data not published. Message saved to cache.");
            }
        } catch (CriticalBusinessException e) {
            log.debug("Could not publish data. There was a critical business exception. {}", e.getErrorMessage());
            messageCache.put(publishNonTelemetryDataParameters.getExternalEndpointId(), messagingIntegrationParameters);
            businessOperationLogService.log(new EndpointLogInformation(publishNonTelemetryDataParameters.getExternalEndpointId(), NA), "Non telemetry data not published. Message saved to cache.");
        }
    }


    private boolean checkConnectionForEndpoint(String externalEndpointId) {
        final var optionalEndpoint = endpointService.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            return null != endpoint.getEndpointStatus() && endpoint.getEndpointStatus().getConnectionState().isConnected();
        } else {
            log.warn("Could not find endpoint with external endpoint ID: {}", externalEndpointId);
            log.info("This might be because the endpoint is not yet registered. The message is cached and will be sent when the endpoint is registered / connected.");
            return false;
        }
    }

    private ByteString asByteString(String base64EncodedMessageContent) {
        return ByteString.copyFrom(Base64.getDecoder().decode(base64EncodedMessageContent));
    }
}
