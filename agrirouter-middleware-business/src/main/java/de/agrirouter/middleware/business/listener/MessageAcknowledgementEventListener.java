package de.agrirouter.middleware.business.listener;

import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.enums.ContentMessageType;
import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorKey;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.ActivateDeviceEvent;
import de.agrirouter.middleware.api.events.MessageAcknowledgementEvent;
import de.agrirouter.middleware.api.events.UpdateSubscriptionsForEndpointEvent;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.business.cache.cloud.CloudOnboardingFailureCache;
import de.agrirouter.middleware.business.events.CloudOffboardingEvent;
import de.agrirouter.middleware.integration.ack.DynamicMessageProperties;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Service to handle the message acknowledgement events.
 */
@Slf4j
@Component
public class MessageAcknowledgementEventListener {

    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final EndpointService endpointService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DecodeMessageService decodeMessageService;
    private final CloudOnboardingFailureCache cloudOnboardingFailureCache;

    public MessageAcknowledgementEventListener(MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                               EndpointService endpointService,
                                               ApplicationEventPublisher applicationEventPublisher,
                                               DecodeMessageService decodeMessageService,
                                               CloudOnboardingFailureCache cloudOnboardingFailureCache) {
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.endpointService = endpointService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.decodeMessageService = decodeMessageService;
        this.cloudOnboardingFailureCache = cloudOnboardingFailureCache;
    }

    /**
     * Handling the message acknowledgement event.
     *
     * @param messageAcknowledgementEvent -
     */
    @EventListener
    public void handle(MessageAcknowledgementEvent messageAcknowledgementEvent) {
        log.debug("Handling the message acknowledgement event.");
        final var optionalMessageWaitingForAcknowledgement = messageWaitingForAcknowledgementService.findByMessageId(messageAcknowledgementEvent.getDecodedMessageResponse().getResponseEnvelope().getApplicationMessageId());
        if (optionalMessageWaitingForAcknowledgement.isPresent()) {
            final var messageWaitingForAcknowledgement = optionalMessageWaitingForAcknowledgement.get();
            final var decodedMessageResponse = messageAcknowledgementEvent.getDecodedMessageResponse();
            switch (decodedMessageResponse.getResponseEnvelope().getType()) {
                case ACK, ACK_FOR_FEED_MESSAGE, ACK_FOR_FEED_HEADER_LIST, CLOUD_REGISTRATIONS -> {
                    handleSuccessMessage(messageWaitingForAcknowledgement);
                    if (SystemMessageType.DKE_CAPABILITIES.getKey().equals(messageWaitingForAcknowledgement.getTechnicalMessageType())) {
                        applicationEventPublisher.publishEvent(new UpdateSubscriptionsForEndpointEvent(this, messageWaitingForAcknowledgement.getAgrirouterEndpointId()));
                    }
                    if (ContentMessageType.ISO_11783_DEVICE_DESCRIPTION.getKey().equals(messageWaitingForAcknowledgement.getTechnicalMessageType())) {
                        applicationEventPublisher.publishEvent(new ActivateDeviceEvent(this, messageWaitingForAcknowledgement.getDynamicPropertyAsString(DynamicMessageProperties.TEAM_SET_CONTEXT_ID)));
                    }
                    if (SystemMessageType.DKE_CLOUD_OFFBOARD_ENDPOINTS.getKey().equals(messageWaitingForAcknowledgement.getTechnicalMessageType())) {
                        applicationEventPublisher.publishEvent(new CloudOffboardingEvent(this, messageWaitingForAcknowledgement.getDynamicPropertyAsStringList(DynamicMessageProperties.EXTERNAL_VIRTUAL_ENDPOINT_IDS)));
                    }
                }
                case ACK_WITH_MESSAGES -> {
                    handleSuccessMessageAndUpdateWarnings(decodedMessageResponse, messageWaitingForAcknowledgement);
                    if (SystemMessageType.DKE_CAPABILITIES.getKey().equals(messageWaitingForAcknowledgement.getTechnicalMessageType())) {
                        applicationEventPublisher.publishEvent(new UpdateSubscriptionsForEndpointEvent(this, messageWaitingForAcknowledgement.getAgrirouterEndpointId()));
                    }
                    if (ContentMessageType.ISO_11783_DEVICE_DESCRIPTION.getKey().equals(messageWaitingForAcknowledgement.getTechnicalMessageType())) {
                        applicationEventPublisher.publishEvent(new ActivateDeviceEvent(this, messageWaitingForAcknowledgement.getDynamicPropertyAsString(DynamicMessageProperties.TEAM_SET_CONTEXT_ID)));
                    }
                }
                case ACK_WITH_FAILURE -> {
                    handleErrorMessage(decodedMessageResponse, messageWaitingForAcknowledgement);
                    final var messages = decodeMessageService.decode(decodedMessageResponse.getResponsePayloadWrapper().getDetails());
                    final var message = messages.getMessages(0);
                    if (ContentMessageType.ISO_11783_DEVICE_DESCRIPTION.getKey().equals(messageWaitingForAcknowledgement.getTechnicalMessageType()) && message.getMessageCode().equals("VAL_000004")) {
                        log.debug("Looks like there are no recipients for the device description. But the AR received the device description and it was valid. Trigger activation.");
                        applicationEventPublisher.publishEvent(new ActivateDeviceEvent(this, messageWaitingForAcknowledgement.getDynamicPropertyAsString(DynamicMessageProperties.TEAM_SET_CONTEXT_ID)));
                    }
                    if (SystemMessageType.DKE_CLOUD_OFFBOARD_ENDPOINTS.getKey().equals(messageWaitingForAcknowledgement.getTechnicalMessageType()) && message.getMessageCode().equals("VAL_000011")) {
                        log.warn("Looks like this was a cloud offboarding for a non existing endpoint. This is not an error. The endpoint is deleted in the AR.");
                        try {
                            var endpointIdForTheVirtualEndpoint = message.getArgsMap().get("endpointId");
                            if (null != endpointIdForTheVirtualEndpoint) {
                                endpointService.deleteEndpointDataFromTheMiddlewareByAgrirouterId(endpointIdForTheVirtualEndpoint);
                            } else {
                                log.warn("The endpointId within the message is null. There is no possibility to delete the endpoint from the middleware.");
                            }
                        } catch (BusinessException e) {
                            log.debug("The endpoint does not exist anymore. There is nothing to do.");
                        }
                    }
                }
            }
            messageWaitingForAcknowledgementService.delete(messageWaitingForAcknowledgement);
        } else {
            log.error(ErrorMessageFactory.couldNotFindMessageWaitingForAcknowledgement(messageAcknowledgementEvent.getDecodedMessageResponse().getResponseEnvelope().getApplicationMessageId()).asLogMessage());
        }

    }

    private void handleSuccessMessage(MessageWaitingForAcknowledgement messageWaitingForAcknowledgement) {
        log.debug("Since the message '{}' for endpoint '{}' had a successful ACK, nothing to do right now. The message waiting for ACK was deleted.", messageWaitingForAcknowledgement.getMessageId(), messageWaitingForAcknowledgement.getAgrirouterEndpointId());
    }

    private void handleSuccessMessageAndUpdateWarnings(DecodeMessageResponse decodedMessageResponse, MessageWaitingForAcknowledgement messageWaitingForAcknowledgement) {
        log.debug("Since the message had a ACK with messages, either the warning or the information is updated. The message waiting for ACK was deleted.");
        try {
            final var endpoint = endpointService.findByAgrirouterEndpointId(messageWaitingForAcknowledgement.getAgrirouterEndpointId());
            if (decodedMessageResponse.getResponseEnvelope().getResponseCode() >= 400) {
                endpointService.updateWarnings(endpoint, decodedMessageResponse);
            }
        } catch (BusinessException e) {
            log.error(e.getErrorMessage().asLogMessage());
        }
    }

    private void handleErrorMessage(DecodeMessageResponse decodedMessageResponse, MessageWaitingForAcknowledgement messageWaitingForAcknowledgement) {
        log.debug("Since the message had a ACK with failure, the errors are updated. The message waiting for ACK was deleted.");
        try {
            final var endpoint = endpointService.findByAgrirouterEndpointId(messageWaitingForAcknowledgement.getAgrirouterEndpointId());
            if (SystemMessageType.DKE_CLOUD_ONBOARD_ENDPOINTS.getKey().equals(messageWaitingForAcknowledgement.getTechnicalMessageType())) {
                log.debug("Looks like this was a cloud onboarding message. There has to be a failure for the virtual endpoint ID.");
                final var externalVirtualEndpointId = messageWaitingForAcknowledgement.getDynamicPropertyAsString(DynamicMessageProperties.EXTERNAL_VIRTUAL_ENDPOINT_ID);
                if (StringUtils.isNotBlank(externalVirtualEndpointId)) {
                    cloudOnboardingFailureCache.put(endpoint.getExternalEndpointId(), externalVirtualEndpointId, ErrorKey.UNKNOWN_ERROR.getKey(), "There was an error while sending the message to the agrirouter. Please check if the endpoint is available.");
                } else {
                    log.error("The external virtual endpoint ID is blank. This should not happen.");
                }
            }
            endpointService.updateErrors(endpoint, decodedMessageResponse);
        } catch (BusinessException e) {
            log.error(e.getErrorMessage().asLogMessage());
        }
    }

}
