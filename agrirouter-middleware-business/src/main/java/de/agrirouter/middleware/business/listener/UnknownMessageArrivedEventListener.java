package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.events.UnknownMessageEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.domain.UnprocessedMessage;
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
    private final EndpointService endpointService;
    private final BusinessOperationLogService businessOperationLogService;

    public UnknownMessageArrivedEventListener(UnprocessedMessageRepository unprocessedMessageRepository,
                                              EndpointService endpointService,
                                              BusinessOperationLogService businessOperationLogService) {
        this.unprocessedMessageRepository = unprocessedMessageRepository;
        this.endpointService = endpointService;
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
        try {
            final var endpoint = endpointService.findByAgrirouterEndpointId(unknownMessageArrivedEvent.getFetchMessageResponse().getSensorAlternateId());
            UnprocessedMessage unprocessedMessage = new UnprocessedMessage();
            unprocessedMessage.setAgrirouterEndpointId(unknownMessageArrivedEvent.getFetchMessageResponse().getSensorAlternateId());
            unprocessedMessage.setMessage(unknownMessageArrivedEvent.getFetchMessageResponse().getCommand().getMessage());
            unprocessedMessageRepository.save(unprocessedMessage);
            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Unknown message arrived. The message has been saved in the unprocessed message repository.");
        } catch (BusinessException e) {
            log.error(e.getErrorMessage().asLogMessage());
        }
    }

}
