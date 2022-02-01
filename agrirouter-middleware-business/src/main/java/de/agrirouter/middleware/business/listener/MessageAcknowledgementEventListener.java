package de.agrirouter.middleware.business.listener;

import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.ActivateDeviceEvent;
import de.agrirouter.middleware.api.events.MessageAcknowledgementEvent;
import de.agrirouter.middleware.api.events.UpdateSubscriptionsForEndpointEvent;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.integration.ack.DynamicMessageProperties;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.persistence.EndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Service to handle the message acknowledgement events.
 */
@Component
public class MessageAcknowledgementEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageAcknowledgementEventListener.class);

    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final EndpointRepository endpointRepository;
    private final EndpointService endpointService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DecodeMessageService decodeMessageService;

    public MessageAcknowledgementEventListener(MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                               EndpointRepository endpointRepository,
                                               EndpointService endpointService,
                                               ApplicationEventPublisher applicationEventPublisher,
                                               DecodeMessageService decodeMessageService) {
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.endpointRepository = endpointRepository;
        this.endpointService = endpointService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.decodeMessageService = decodeMessageService;
    }

    /**
     * Handling the message acknowledgement event.
     *
     * @param messageAcknowledgementEvent -
     */
    @EventListener
    public void handle(MessageAcknowledgementEvent messageAcknowledgementEvent) {
        LOGGER.debug("Handling the message acknowledgement event.");
        final var optionalMessageWaitingForAcknowledgement = messageWaitingForAcknowledgementService.findByMessageId(messageAcknowledgementEvent.getDecodedMessageResponse().getResponseEnvelope().getApplicationMessageId());
        if (optionalMessageWaitingForAcknowledgement.isPresent()) {
            final var messageWaitingForAcknowledgement = optionalMessageWaitingForAcknowledgement.get();
            final var decodedMessageResponse = messageAcknowledgementEvent.getDecodedMessageResponse();
            switch (decodedMessageResponse.getResponseEnvelope().getType()) {
                case ACK:
                case ACK_FOR_FEED_MESSAGE:
                case ACK_FOR_FEED_HEADER_LIST:
                case CLOUD_REGISTRATIONS:
                    handleSuccessMessage(messageWaitingForAcknowledgement);
                    if (TechnicalMessageType.DKE_CAPABILITIES.getKey().equals(messageWaitingForAcknowledgement.getTechnicalMessageType())) {
                        applicationEventPublisher.publishEvent(new UpdateSubscriptionsForEndpointEvent(this, messageWaitingForAcknowledgement.getAgrirouterEndpointId()));
                    }
                    if (TechnicalMessageType.ISO_11783_DEVICE_DESCRIPTION.getKey().equals(messageWaitingForAcknowledgement.getTechnicalMessageType())) {
                        applicationEventPublisher.publishEvent(new ActivateDeviceEvent(this, messageWaitingForAcknowledgement.getDynamicPropertyAsString(DynamicMessageProperties.TEAM_SET_CONTEXT_ID)));
                    }
                    break;
                case ACK_WITH_MESSAGES:
                    handleSuccessMessageAndUpdateWarnings(decodedMessageResponse, messageWaitingForAcknowledgement);
                    if (TechnicalMessageType.DKE_CAPABILITIES.getKey().equals(messageWaitingForAcknowledgement.getTechnicalMessageType())) {
                        applicationEventPublisher.publishEvent(new UpdateSubscriptionsForEndpointEvent(this, messageWaitingForAcknowledgement.getAgrirouterEndpointId()));
                    }
                    if (TechnicalMessageType.ISO_11783_DEVICE_DESCRIPTION.getKey().equals(messageWaitingForAcknowledgement.getTechnicalMessageType())) {
                        applicationEventPublisher.publishEvent(new ActivateDeviceEvent(this, messageWaitingForAcknowledgement.getDynamicPropertyAsString(DynamicMessageProperties.TEAM_SET_CONTEXT_ID)));
                    }
                    break;
                case ACK_WITH_FAILURE:
                    handleErrorMessage(decodedMessageResponse, messageWaitingForAcknowledgement);
                    final var messages = decodeMessageService.decode(decodedMessageResponse.getResponsePayloadWrapper().getDetails());
                    final var message = messages.getMessages(0);
                    if (TechnicalMessageType.ISO_11783_DEVICE_DESCRIPTION.getKey().equals(messageWaitingForAcknowledgement.getTechnicalMessageType()) && message.getMessageCode().equals("VAL_000004")) {
                        LOGGER.debug("Looks like there are no recipients for the device description. But the AR received the device description and it was valid. Trigger activation.");
                        applicationEventPublisher.publishEvent(new ActivateDeviceEvent(this, messageWaitingForAcknowledgement.getDynamicPropertyAsString(DynamicMessageProperties.TEAM_SET_CONTEXT_ID)));
                    }
                    break;
            }
            messageWaitingForAcknowledgementService.delete(messageWaitingForAcknowledgement);
        } else {
            LOGGER.error(ErrorMessageFactory.couldNotFindMessageWaitingForAcknowledgement(messageAcknowledgementEvent.getDecodedMessageResponse().getResponseEnvelope().getApplicationMessageId()).asLogMessage());
        }

    }

    private void handleSuccessMessage(MessageWaitingForAcknowledgement messageWaitingForAcknowledgement) {
        LOGGER.debug("Since the message had a successful ACK, nothing to do right now. The message waiting for ACK was deleted.");
        final var optional = endpointRepository.findByAgrirouterEndpointId(messageWaitingForAcknowledgement.getAgrirouterEndpointId());
        if (optional.isEmpty()) {
            LOGGER.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
        }
    }

    private void handleSuccessMessageAndUpdateWarnings(DecodeMessageResponse decodedMessageResponse, MessageWaitingForAcknowledgement messageWaitingForAcknowledgement) {
        LOGGER.debug("Since the message had a ACK with messages, either the warning or the information is updated. The message waiting for ACK was deleted.");
        final var optional = endpointRepository.findByAgrirouterEndpointId(messageWaitingForAcknowledgement.getAgrirouterEndpointId());
        if (optional.isPresent()) {
            final var endpoint = optional.get();
            if (decodedMessageResponse.getResponseEnvelope().getResponseCode() >= 400) {
                endpointService.updateWarnings(endpoint, decodedMessageResponse);
            } else {
                endpointService.updateInformation(endpoint, decodedMessageResponse);
            }
        } else {
            LOGGER.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
        }
    }

    private void handleErrorMessage(DecodeMessageResponse decodedMessageResponse, MessageWaitingForAcknowledgement messageWaitingForAcknowledgement) {
        LOGGER.debug("Since the message had a ACK with failure, the errors are updated. The message waiting for ACK was deleted.");
        final var optional = endpointRepository.findByAgrirouterEndpointId(messageWaitingForAcknowledgement.getAgrirouterEndpointId());
        if (optional.isPresent()) {
            final var endpoint = optional.get();
            endpointService.updateErrors(endpoint, decodedMessageResponse);
        } else {
            LOGGER.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
        }
    }

}
