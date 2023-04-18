package de.agrirouter.middleware.business.listener;

import agrirouter.response.payload.account.Endpoints;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.google.protobuf.InvalidProtocolBufferException;
import de.agrirouter.middleware.api.events.UpdateRecipientsForEndpointEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.cache.events.BusinessEvent;
import de.agrirouter.middleware.business.cache.events.BusinessEventApplicationEvent;
import de.agrirouter.middleware.business.cache.events.BusinessEventType;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.MessageRecipient;
import de.agrirouter.middleware.persistence.EndpointRepository;
import de.agrirouter.middleware.persistence.MessageRecipientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final ApplicationEventPublisher applicationEventPublisher;

    public UpdateRecipientsForEndpointEventListener(EndpointRepository endpointRepository,
                                                    DecodeMessageService decodeMessageService,
                                                    MessageRecipientRepository messageRecipientRepository,
                                                    BusinessOperationLogService businessOperationLogService,
                                                    ApplicationEventPublisher applicationEventPublisher) {
        this.endpointRepository = endpointRepository;
        this.messageRecipientRepository = messageRecipientRepository;
        this.decodeMessageService = decodeMessageService;
        this.businessOperationLogService = businessOperationLogService;
        this.applicationEventPublisher = applicationEventPublisher;
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
                final var fetchMessageResponse = updateRecipientsForEndpointEvent.getFetchMessageResponse();
                final var decodedMessageResponse = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());
                final var listEndpointsResponse = Endpoints.ListEndpointsResponse.parseFrom(decodedMessageResponse.getResponsePayloadWrapper().getDetails().getValue());
                if (checkIfThereAreOtherRecipientsThanBefore(endpoint, listEndpointsResponse)
                        || checkIfAnUpdateOfTheRecipientsIsNeeded(endpoint, listEndpointsResponse)) {
                    log.debug("Remove all of the former message recipients.");
                    messageRecipientRepository.deleteAll(endpoint.getMessageRecipients());
                    endpoint.setMessageRecipients(new HashSet<>());
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
                    applicationEventPublisher.publishEvent(new BusinessEventApplicationEvent(this, endpoint.getExternalEndpointId(), new BusinessEvent(Instant.now(), BusinessEventType.RECIPIENTS_UPDATED)));
                }
            } else {
                log.warn("There is no change within the recipients of the endpoint. Therefore, the recipients were not updated.");
            }
        } else {
            log.warn("The endpoint was not found in the database, the message was deleted but not saved.");
        }
    }

    private boolean checkIfThereAreOtherRecipientsThanBefore(Endpoint endpoint, Endpoints.ListEndpointsResponse listEndpointsResponse) {
        var otherRecipientsThanBefore = new AtomicBoolean(false);
        for (Endpoints.ListEndpointsResponse.Endpoint e : listEndpointsResponse.getEndpointsList()) {
            if (otherRecipientsThanBefore.get()) {
                break;
            }
            var thereIsAMessageRecipientWithTheSameValues = isThereAMessageRecipientWithTheSameId(endpoint, e);
            if (!thereIsAMessageRecipientWithTheSameValues) {
                otherRecipientsThanBefore.set(true);
            }
        }
        return otherRecipientsThanBefore.get();
    }

    private boolean isThereAMessageRecipientWithTheSameId(Endpoint endpoint, Endpoints.ListEndpointsResponse.Endpoint e) {
        var thereIsAMessageRecipientWithTheSameValues = new AtomicBoolean(false);
        endpoint.getMessageRecipients().stream().filter(mr -> compareTheMessageRecipientIds(mr, e)).findFirst().ifPresentOrElse(
                messageRecipient -> {
                    log.debug("The recipient '{}' is already known.", messageRecipient.getAgrirouterEndpointId());
                    thereIsAMessageRecipientWithTheSameValues.set(true);
                },
                () -> log.debug("The recipient '{}' is not known, therefore an update is needed.", e.getEndpointId())
        );
        return thereIsAMessageRecipientWithTheSameValues.get();
    }

    private boolean compareTheMessageRecipientIds(MessageRecipient mr, Endpoints.ListEndpointsResponse.Endpoint e) {
        return mr.getAgrirouterEndpointId().equals(e.getEndpointId());
    }

    private boolean checkIfAnUpdateOfTheRecipientsIsNeeded(Endpoint endpoint, Endpoints.ListEndpointsResponse listEndpointsResponse) {
        var updateNeeded = new AtomicBoolean(false);
        for (Endpoints.ListEndpointsResponse.Endpoint e : listEndpointsResponse.getEndpointsList()) {
            if (updateNeeded.get()) {
                break;
            }
            for (Endpoints.ListEndpointsResponse.MessageType mt : e.getMessageTypesList()) {
                var thereAMessageRecipientWithTheSameValues = isThereAMessageRecipientWithTheSameValues(endpoint, e, mt);
                if (!thereAMessageRecipientWithTheSameValues) {
                    updateNeeded.set(true);
                    break;
                }
            }
        }
        return updateNeeded.get();
    }

    private boolean isThereAMessageRecipientWithTheSameValues(Endpoint endpoint, Endpoints.ListEndpointsResponse.Endpoint e, Endpoints.ListEndpointsResponse.MessageType mt) {
        var thereIsAMessageRecipientWithTheSameValues = new AtomicBoolean(false);
        endpoint.getMessageRecipients().stream().filter(mr -> compareTheMessageRecipientAttributes(mr, e, mt)).findFirst().ifPresentOrElse(
                messageRecipient -> {
                    log.debug("The recipient '{}' is already known.", messageRecipient.getAgrirouterEndpointId());
                    thereIsAMessageRecipientWithTheSameValues.set(true);
                },
                () -> log.debug("The recipient '{}' is not known yet, therefore an update is needed.", e.getEndpointId())
        );
        return thereIsAMessageRecipientWithTheSameValues.get();
    }

    private boolean compareTheMessageRecipientAttributes(MessageRecipient mr, Endpoints.ListEndpointsResponse.Endpoint e, Endpoints.ListEndpointsResponse.MessageType mt) {
        return mr.getAgrirouterEndpointId().equals(e.getEndpointId()) &&
                mr.getEndpointName().equals(e.getEndpointName()) &&
                mr.getEndpointType().equals(e.getEndpointType()) &&
                mr.getExternalId().equals(e.getExternalId()) &&
                mr.getTechnicalMessageType().equals(mt.getTechnicalMessageType()) &&
                mr.getDirection().equals(mt.getDirection().name());
    }

}
