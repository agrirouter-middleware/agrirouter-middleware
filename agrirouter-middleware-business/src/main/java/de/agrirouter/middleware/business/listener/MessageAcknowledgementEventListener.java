package de.agrirouter.middleware.business.listener;

import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.enums.ContentMessageType;
import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.ActivateDeviceEvent;
import de.agrirouter.middleware.api.events.MessageAcknowledgementEvent;
import de.agrirouter.middleware.api.events.UpdateSubscriptionsForEndpointEvent;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.business.cache.messaging.MessageCache;
import de.agrirouter.middleware.integration.ack.DynamicMessageProperties;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.persistence.EndpointRepository;
import lombok.extern.slf4j.Slf4j;
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
    private final EndpointRepository endpointRepository;
    private final EndpointService endpointService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DecodeMessageService decodeMessageService;

    private final MessageCache messageCache;

    public MessageAcknowledgementEventListener(MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                               EndpointRepository endpointRepository,
                                               EndpointService endpointService,
                                               ApplicationEventPublisher applicationEventPublisher,
                                               DecodeMessageService decodeMessageService,
                                               MessageCache messageCache) {
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.endpointRepository = endpointRepository;
        this.endpointService = endpointService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.decodeMessageService = decodeMessageService;
        this.messageCache = messageCache;
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
                }
            }
            messageWaitingForAcknowledgementService.delete(messageWaitingForAcknowledgement);
        } else {
            log.error(ErrorMessageFactory.couldNotFindMessageWaitingForAcknowledgement(messageAcknowledgementEvent.getDecodedMessageResponse().getResponseEnvelope().getApplicationMessageId()).asLogMessage());
        }

    }

    private void handleSuccessMessage(MessageWaitingForAcknowledgement messageWaitingForAcknowledgement) {
        log.debug("Since the message had a successful ACK, nothing to do right now. The message waiting for ACK was deleted.");
        final var optional = endpointRepository.findByAgrirouterEndpointId(messageWaitingForAcknowledgement.getAgrirouterEndpointId());
        if (optional.isEmpty()) {
            log.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
        }
    }

    private void handleSuccessMessageAndUpdateWarnings(DecodeMessageResponse decodedMessageResponse, MessageWaitingForAcknowledgement messageWaitingForAcknowledgement) {
        log.debug("Since the message had a ACK with messages, either the warning or the information is updated. The message waiting for ACK was deleted.");
        final var optional = endpointRepository.findByAgrirouterEndpointId(messageWaitingForAcknowledgement.getAgrirouterEndpointId());
        if (optional.isPresent()) {
            final var endpoint = optional.get();
            if (decodedMessageResponse.getResponseEnvelope().getResponseCode() >= 400) {
                endpointService.updateWarnings(endpoint, decodedMessageResponse);
            }
        } else {
            log.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
        }
    }

    private void handleErrorMessage(DecodeMessageResponse decodedMessageResponse, MessageWaitingForAcknowledgement messageWaitingForAcknowledgement) {
        log.debug("Since the message had a ACK with failure, the errors are updated. The message waiting for ACK was deleted.");
        final var optional = endpointRepository.findByAgrirouterEndpointId(messageWaitingForAcknowledgement.getAgrirouterEndpointId());
        if (optional.isPresent()) {
            final var endpoint = optional.get();
            endpointService.updateErrors(endpoint, decodedMessageResponse);
        } else {
            log.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
        }
    }

}
