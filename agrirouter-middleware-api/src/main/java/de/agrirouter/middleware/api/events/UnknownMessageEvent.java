package de.agrirouter.middleware.api.events;

import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that will be thrown if there is any unknown message that has to be handled.
 */
@Getter
public class UnknownMessageEvent extends ApplicationEvent {

    /**
     * The response from the AR, that has to be handled.
     */
    private final FetchMessageResponse fetchMessageResponse;

    public UnknownMessageEvent(Object source, FetchMessageResponse fetchMessageResponse) {
        super(source);
        this.fetchMessageResponse = fetchMessageResponse;
    }

}
