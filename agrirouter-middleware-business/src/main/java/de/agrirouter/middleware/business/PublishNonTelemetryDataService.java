package de.agrirouter.middleware.business;

import com.google.protobuf.ByteString;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.CriticalBusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.cache.messaging.MessageCache;
import de.agrirouter.middleware.business.events.ResendMessageCacheEntryEvent;
import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;
import de.agrirouter.middleware.integration.SendMessageIntegrationService;
import de.agrirouter.middleware.integration.parameters.MessagingIntegrationParameters;
import de.agrirouter.middleware.integration.status.AgrirouterStatusIntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
            if (endpointService.isHealthy(publishNonTelemetryDataParameters.getExternalEndpointId())) {
                checkAndUpdateRecipients(publishNonTelemetryDataParameters);
                var endpoint = endpointService.findByExternalEndpointId(publishNonTelemetryDataParameters.getExternalEndpointId());
                sendMessageIntegrationService.publish(endpoint, messagingIntegrationParameters);
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

    private void checkAndUpdateRecipients(PublishNonTelemetryDataParameters publishNonTelemetryDataParameters) {
        if (null != publishNonTelemetryDataParameters.getRecipients() && !publishNonTelemetryDataParameters.getRecipients().isEmpty()) {
            try {
                final var endpoint = endpointService.findByExternalEndpointId(publishNonTelemetryDataParameters.getExternalEndpointId());
                var messageRecipients = endpointService.getMessageRecipients(endpoint.getExternalEndpointId());
                var updatedMessageRecipients = new ArrayList<String>();
                publishNonTelemetryDataParameters.getRecipients().forEach(recipient -> messageRecipients.stream()
                        .filter(messageRecipient -> StringUtils.equals(recipient, messageRecipient.getAgrirouterEndpointId()) || StringUtils.equals(recipient, messageRecipient.getExternalId()))
                        .findFirst().ifPresentOrElse(messageRecipient -> {
                            log.debug("Recipient {} does exists for endpoint {}, using the agrirouter endpoint ID to send the message.", recipient, publishNonTelemetryDataParameters.getExternalEndpointId());
                            updatedMessageRecipients.add(messageRecipient.getAgrirouterEndpointId());
                        }, () -> log.warn("Recipient {} does not exist for endpoint {}.", recipient, publishNonTelemetryDataParameters.getExternalEndpointId())));
                log.debug("Former recipients for endpoint {}: {}", publishNonTelemetryDataParameters.getExternalEndpointId(), publishNonTelemetryDataParameters.getRecipients());
                log.debug("Updated recipients for endpoint {}: {}", publishNonTelemetryDataParameters.getExternalEndpointId(), updatedMessageRecipients);
                publishNonTelemetryDataParameters.setRecipients(updatedMessageRecipients);
            } catch (BusinessException e) {
                log.error(e.getErrorMessage().asLogMessage());
                log.warn("Could not find endpoint with external endpoint ID: {}", publishNonTelemetryDataParameters.getExternalEndpointId());
                log.info("This might be because the endpoint is not yet registered. The recipients are not updated.");
            }
        }
    }

    private ByteString asByteString(String base64EncodedMessageContent) {
        try {
            return ByteString.copyFrom(Base64.getDecoder().decode(base64EncodedMessageContent));
        } catch (IllegalArgumentException e) {
            log.debug("Could not decode base64 encoded message content.");
            log.trace("Message content: {}", base64EncodedMessageContent);
            throw new BusinessException(ErrorMessageFactory.couldNotDecodeBase64EncodedMessageContent());
        }
    }
}
