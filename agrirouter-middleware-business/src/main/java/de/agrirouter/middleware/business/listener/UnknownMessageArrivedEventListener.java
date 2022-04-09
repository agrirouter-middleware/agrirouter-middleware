package de.agrirouter.middleware.business.listener;

import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.UnknownMessageEvent;
import de.agrirouter.middleware.domain.UnprocessedMessage;
import de.agrirouter.middleware.persistence.EndpointRepository;
import de.agrirouter.middleware.persistence.UnprocessedMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Service to handle the unknown message events.
 */
@Component
public class UnknownMessageArrivedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnknownMessageArrivedEventListener.class);

    private final UnprocessedMessageRepository unprocessedMessageRepository;
    private final EndpointRepository endpointRepository;

    public UnknownMessageArrivedEventListener(UnprocessedMessageRepository unprocessedMessageRepository,
                                              EndpointRepository endpointRepository) {
        this.unprocessedMessageRepository = unprocessedMessageRepository;
        this.endpointRepository = endpointRepository;
    }

    /**
     * Handling the unknown message event.
     *
     * @param unknownMessageArrivedEvent -
     */
    @EventListener

    public void unknownMessageArrived(UnknownMessageEvent unknownMessageArrivedEvent) {
        LOGGER.debug("There has been an unknown message that has to be handled.");
        final var optional = endpointRepository.findByAgrirouterEndpointId(unknownMessageArrivedEvent.getFetchMessageResponse().getSensorAlternateId());
        if (optional.isPresent()) {
            UnprocessedMessage unprocessedMessage = new UnprocessedMessage();
            unprocessedMessage.setAgrirouterEndpointId(unknownMessageArrivedEvent.getFetchMessageResponse().getSensorAlternateId());
            unprocessedMessage.setMessage(unknownMessageArrivedEvent.getFetchMessageResponse().getCommand().getMessage());
            unprocessedMessageRepository.save(unprocessedMessage);
        } else {
            LOGGER.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
        }
    }

}
