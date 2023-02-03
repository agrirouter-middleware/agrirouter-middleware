package de.agrirouter.middleware.business.cache.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Cache root object.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessEvent {
    private Instant timestamp;

    private BusinessEventType eventType;

}
