package de.agrirouter.middleware.business.events;

import de.agrirouter.middleware.business.parameters.PublishNonTelemetryDataParameters;
import org.springframework.context.ApplicationEvent;

/**
 * Will be published if a message from the cache should be resent.
 */
public class ResendMessageCacheEntryEvent extends ApplicationEvent {
    private final PublishNonTelemetryDataParameters publishNonTelemetryDataParameters;

    public ResendMessageCacheEntryEvent(Object source, PublishNonTelemetryDataParameters publishNonTelemetryDataParameters) {
        super(source);
        this.publishNonTelemetryDataParameters = publishNonTelemetryDataParameters;
    }

    public PublishNonTelemetryDataParameters getPublishNonTelemetryDataParameters() {
        return publishNonTelemetryDataParameters;
    }
}
