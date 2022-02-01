package de.agrirouter.middleware.domain.log;

import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.BaseEntity;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.enums.BusinessLogEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

/**
 * A business log event for the application.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class BusinessLogEvent extends BaseEntity {

    /**
     * The application causing the event.
     */
    @ManyToOne
    private Application application;

    /**
     * The endpoint causing the event.
     */
    @ManyToOne
    private Endpoint endpoint;

    /**
     * The type of event.
     */
    @Enumerated(EnumType.STRING)
    private BusinessLogEventType businessLogEventType;

    /**
     * The internal message to make the status readable.
     */
    @Lob
    private String message;

}
