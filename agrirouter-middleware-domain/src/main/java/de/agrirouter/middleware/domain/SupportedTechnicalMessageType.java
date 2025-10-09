package de.agrirouter.middleware.domain;

import agrirouter.request.payload.endpoint.Capabilities.CapabilitySpecification.Direction;
import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A supported technical message type.
 * Every technical message type has a direction and a agrirouter© specific technical message type.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class SupportedTechnicalMessageType extends BaseEntity {

    /**
     * The technical message type, that the application does support, i.e. TaskData, EFDI, etc.
     */
    @Enumerated(EnumType.STRING)
    private TemporaryContentMessageType technicalMessageType;

    /**
     * The direction the message type can be handled, i.e. SEND, RECEIVE, SEND_RECEIVE.
     */
    @Enumerated(EnumType.STRING)
    private Direction direction;

}
