package de.agrirouter.middleware.business.cache.events;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Cache root object.
 */
@Getter
@Setter
public class BusinessEvent {
    private Instant timestamp;

    private BusinessLogEventType eventType;

}
