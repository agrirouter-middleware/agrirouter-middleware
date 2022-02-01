package de.agrirouter.middleware.api.events;

import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * This event is thrown if any push message has arrived.
 */
@Getter
public class PushMessageEvent extends ApplicationEvent {

    /**
     * The response from the AR, that has to be handled.
     */
    private final FetchMessageResponse fetchMessageResponse;

    public PushMessageEvent(Object source, FetchMessageResponse fetchMessageResponse) {
        super(source);
        this.fetchMessageResponse = fetchMessageResponse;
    }
    
}
