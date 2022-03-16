package de.agrirouter.middleware.api.events;

import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * The event is fired in case of the retrieval of an endpoint listing.
 */
@Getter
public class UpdateRecipientsForEndpointEvent extends ApplicationEvent {

    /**
     * The agrirouter© ID of the endpoint.
     */
    private final String agrirouterEndpointId;

    /**
     * The response from the AR, that has to be handled.
     */
    private final FetchMessageResponse fetchMessageResponse;

    public UpdateRecipientsForEndpointEvent(Object source, String agrirouterEndpointId, FetchMessageResponse fetchMessageResponse) {
        super(source);
        this.agrirouterEndpointId = agrirouterEndpointId;
        this.fetchMessageResponse = fetchMessageResponse;
    }

}
