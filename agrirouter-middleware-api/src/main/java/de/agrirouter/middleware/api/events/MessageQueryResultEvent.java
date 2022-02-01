package de.agrirouter.middleware.api.events;

import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * This event is thrown if any message query result has arrived.
 */
@Getter
public class MessageQueryResultEvent extends ApplicationEvent {

    /**
     * The response from the AR, that has to be handled.
     */
    private final FetchMessageResponse fetchMessageResponse;

    public MessageQueryResultEvent(Object source, FetchMessageResponse fetchMessageResponse) {
        super(source);
        this.fetchMessageResponse = fetchMessageResponse;
    }

}
