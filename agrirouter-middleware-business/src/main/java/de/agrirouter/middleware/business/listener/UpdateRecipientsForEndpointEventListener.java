package de.agrirouter.middleware.business.listener;

import agrirouter.response.payload.account.Endpoints;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.google.protobuf.InvalidProtocolBufferException;
import de.agrirouter.middleware.api.events.UpdateRecipientsForEndpointEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.domain.MessageRecipient;
import de.agrirouter.middleware.persistence.EndpointRepository;
import de.agrirouter.middleware.persistence.MessageRecipientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashSet;

/**
 * Service to update the endpoint status.
 */
@Slf4j
@Service
public class UpdateRecipientsForEndpointEventListener {

    private final EndpointRepository endpointRepository;
    private final DecodeMessageService decodeMessageService;
    private final MessageRecipientRepository messageRecipientRepository;
    private final BusinessOperationLogService businessOperationLogService;

    public UpdateRecipientsForEndpointEventListener(EndpointRepository endpointRepository,
                                                    DecodeMessageService decodeMessageService,
                                                    MessageRecipientRepository messageRecipientRepository,
                                                    BusinessOperationLogService businessOperationLogService) {
        this.endpointRepository = endpointRepository;
        this.messageRecipientRepository = messageRecipientRepository;
        this.decodeMessageService = decodeMessageService;
        this.businessOperationLogService = businessOperationLogService;
    }

    /**
     * Update the recipients of the endpoint after the result for the endpoint list has arrived.
     *
     * @param updateRecipientsForEndpointEvent -
     */
    @EventListener
    public void updateRecipientsForEndpoint(UpdateRecipientsForEndpointEvent updateRecipientsForEndpointEvent) throws InvalidProtocolBufferException {
        log.debug("Update the recipients for the AR endpoint '{}'.", updateRecipientsForEndpointEvent.getAgrirouterEndpointId());
        final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(updateRecipientsForEndpointEvent.getAgrirouterEndpointId());
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            if (null != updateRecipientsForEndpointEvent.getFetchMessageResponse()) {
                log.debug("Remove all of the former message recipients.");
                messageRecipientRepository.deleteAll(endpoint.getMessageRecipients());
                endpoint.setMessageRecipients(new HashSet<>());
                final var fetchMessageResponse = updateRecipientsForEndpointEvent.getFetchMessageResponse();
                final var decodedMessageResponse = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());
                // FIXME Replace this one after updating to the latest release.
                final var listEndpointsResponse = Endpoints.ListEndpointsResponse.parseFrom(decodedMessageResponse.getResponsePayloadWrapper().getDetails().getValue());
                listEndpointsResponse.getEndpointsList().forEach(e -> e.getMessageTypesList().forEach(messageType -> {
                    final var messageRecipient = new MessageRecipient();
                    messageRecipient.setAgrirouterEndpointId(e.getEndpointId());
                    messageRecipient.setEndpointName(e.getEndpointName());
                    messageRecipient.setEndpointType(e.getEndpointType());
                    messageRecipient.setExternalId(e.getExternalId());
                    messageRecipient.setTechnicalMessageType(messageType.getTechnicalMessageType());
                    messageRecipient.setDirection(messageType.getDirection().name());
                    messageRecipientRepository.save(messageRecipient);
                    endpoint.getMessageRecipients().add(messageRecipient);
                }));
                log.debug("There were {} recipients found for the endpoint '{}'.", endpoint.getMessageRecipients().size(), endpoint.getExternalEndpointId());
                log.trace("{}", endpoint.getMessageRecipients());
                endpointRepository.save(endpoint);
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Recipients updated.");
            }
        } else {
            log.warn("The endpoint was not found in the database, the message was deleted but not saved.");
        }
    }

}
