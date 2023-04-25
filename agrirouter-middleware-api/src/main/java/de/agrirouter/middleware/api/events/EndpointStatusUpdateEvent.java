package de.agrirouter.middleware.api.events;

import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * The event is fired in case of a status update.
 */
@Getter
public class EndpointStatusUpdateEvent extends ApplicationEvent {

    /**
     * The agrirouter© ID of the endpoint.
     */
    private final String agrirouterEndpointId;

    /**
     * The response from the AR, that has to be handled.
     */
    private final FetchMessageResponse fetchMessageResponse;

    public EndpointStatusUpdateEvent(Object source, String agrirouterEndpointId, FetchMessageResponse fetchMessageResponse) {
        super(source);
        this.agrirouterEndpointId = agrirouterEndpointId;
        this.fetchMessageResponse = fetchMessageResponse;
    }

    public EndpointStatusUpdateEvent(Object source, String agrirouterEndpointId) {
        super(source);
        this.agrirouterEndpointId = agrirouterEndpointId;
        this.fetchMessageResponse = null;
    }

}
