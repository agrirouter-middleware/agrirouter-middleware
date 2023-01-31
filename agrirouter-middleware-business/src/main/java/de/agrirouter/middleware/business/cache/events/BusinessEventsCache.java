package de.agrirouter.middleware.business.cache.events;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache for business events.
 */
@Slf4j
@Component
@Scope(value = "singleton")
public class BusinessEventsCache {

    /**
     * Cache for business events.
     */
    @Getter
    private final Map<String, Map<BusinessLogEventType, Instant>> cache = new HashMap<>();

    /**
     * Handle the application event.
     *
     * @param event The event.
     */
    @EventListener(BusinessEventApplicationEvent.class)
    public void handle(BusinessEventApplicationEvent event) {
        log.trace("Received business event for endpoint {}.", event.getExternalEndpointId());
        if (cache.containsKey(event.getExternalEndpointId())) {
            cache.get(event.getExternalEndpointId()).put(event.getBusinessEvent().getEventType(), event.getBusinessEvent().getTimestamp());
        } else {
            var businessLogEvents = new HashMap<BusinessLogEventType, Instant>();
            businessLogEvents.put(event.getBusinessEvent().getEventType(), event.getBusinessEvent().getTimestamp());
            cache.put(event.getExternalEndpointId(), businessLogEvents);
        }
    }

}
