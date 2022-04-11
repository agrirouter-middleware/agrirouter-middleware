package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.UnknownMessageEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.domain.UnprocessedMessage;
import de.agrirouter.middleware.persistence.EndpointRepository;
import de.agrirouter.middleware.persistence.UnprocessedMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Service to handle the unknown message events.
 */
@Slf4j
@Component
public class UnknownMessageArrivedEventListener {

    private final UnprocessedMessageRepository unprocessedMessageRepository;
    private final EndpointRepository endpointRepository;
    private final BusinessOperationLogService businessOperationLogService;

    public UnknownMessageArrivedEventListener(UnprocessedMessageRepository unprocessedMessageRepository,
                                              EndpointRepository endpointRepository,
                                              BusinessOperationLogService businessOperationLogService) {
        this.unprocessedMessageRepository = unprocessedMessageRepository;
        this.endpointRepository = endpointRepository;
        this.businessOperationLogService = businessOperationLogService;
    }

    /**
     * Handling the unknown message event.
     *
     * @param unknownMessageArrivedEvent -
     */
    @EventListener

    public void unknownMessageArrived(UnknownMessageEvent unknownMessageArrivedEvent) {
        log.debug("There has been an unknown message that has to be handled.");
        final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(unknownMessageArrivedEvent.getFetchMessageResponse().getSensorAlternateId());
        if (optionalEndpoint.isPresent()) {
            UnprocessedMessage unprocessedMessage = new UnprocessedMessage();
            unprocessedMessage.setAgrirouterEndpointId(unknownMessageArrivedEvent.getFetchMessageResponse().getSensorAlternateId());
            unprocessedMessage.setMessage(unknownMessageArrivedEvent.getFetchMessageResponse().getCommand().getMessage());
            unprocessedMessageRepository.save(unprocessedMessage);
            businessOperationLogService.log(new EndpointLogInformation(optionalEndpoint.get().getExternalEndpointId(), optionalEndpoint.get().getAgrirouterEndpointId()), "Unknown message arrived. The message has been saved in the unprocessed message repository.");
        } else {
            log.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
        }
    }

}
