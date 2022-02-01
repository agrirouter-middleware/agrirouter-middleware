package de.agrirouter.middleware.api.events;

import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * This event is thrown if any cloud registration result has arrived.
 */
@Getter
public class CloudRegistrationEvent extends ApplicationEvent {

    /**
     * The application message ID to fetch the state.
     */
    private final String applicationMessageId;

    /**
     * The response from the AR, that has to be handled.
     */
    private final FetchMessageResponse fetchMessageResponse;

    public CloudRegistrationEvent(Object source, String applicationMessageId, FetchMessageResponse fetchMessageResponse) {
        super(source);
        this.applicationMessageId = applicationMessageId;
        this.fetchMessageResponse = fetchMessageResponse;
    }

}
