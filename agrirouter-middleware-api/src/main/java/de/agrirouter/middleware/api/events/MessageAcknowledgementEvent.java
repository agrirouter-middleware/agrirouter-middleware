package de.agrirouter.middleware.api.events;

import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * This event is thrown if any message ACK is recognized.
 */
@Getter
public class MessageAcknowledgementEvent extends ApplicationEvent {

    /**
     * The decoded message that has to be handled.
     */
    private final DecodeMessageResponse decodedMessageResponse;

    public MessageAcknowledgementEvent(Object source, DecodeMessageResponse decodedMessageResponse) {
        super(source);
        this.decodedMessageResponse = decodedMessageResponse;
    }

}
