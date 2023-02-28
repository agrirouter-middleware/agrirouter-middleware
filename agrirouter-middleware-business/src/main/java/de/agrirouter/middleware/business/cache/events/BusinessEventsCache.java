package de.agrirouter.middleware.business.cache.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cache for business events.
 */
@Slf4j
@Component
public class BusinessEventsCache {

    /**
     * Cache for business events.
     */
    private final Map<String, Map<BusinessEventType, Instant>> cache = new HashMap<>();

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
            var businessLogEvents = new HashMap<BusinessEventType, Instant>();
            businessLogEvents.put(event.getBusinessEvent().getEventType(), event.getBusinessEvent().getTimestamp());
            cache.put(event.getExternalEndpointId(), businessLogEvents);
        }
    }

    /**
     * Get the business events for a given external endpoint id.
     *
     * @param externalEndpointId The external endpoint id.
     * @return The business events.
     */
    public Optional<Map<BusinessEventType, Instant>> get(String externalEndpointId) {
        return Optional.ofNullable(cache.get(externalEndpointId));
    }
}
