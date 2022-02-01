package de.agrirouter.middleware.businesslog;

import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.enums.BusinessLogEventType;
import de.agrirouter.middleware.domain.log.BusinessLogEvent;

import java.nio.charset.StandardCharsets;

/**
 * Builder for the business log events.
 */
public class BusinessLogEventBuilder {

    private final BusinessLogEvent businessLogEvent;

    public BusinessLogEventBuilder() {
        this.businessLogEvent = new BusinessLogEvent();
    }

    public BusinessLogEventBuilder application(Application application) {
        businessLogEvent.setApplication(application);
        return this;
    }

    public BusinessLogEventBuilder endpoint(Endpoint endpoint) {
        businessLogEvent.setEndpoint(endpoint);
        return this;
    }

    public BusinessLogEventBuilder type(BusinessLogEventType businessLogEventType) {
        businessLogEvent.setBusinessLogEventType(businessLogEventType);
        return this;
    }

    public BusinessLogEventBuilder message(String message, Object... values) {
        businessLogEvent.setMessage(String.format(message, values));
        return this;
    }

    public BusinessLogEvent build() {
        return businessLogEvent;
    }
}
