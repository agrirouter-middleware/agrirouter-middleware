package de.agrirouter.middleware.domain;

import agrirouter.request.payload.endpoint.Capabilities.CapabilitySpecification.Direction;
import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * A supported technical message type.
 * Every technical message type has a direction and a agrirouter specific technical message type.
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
    private TechnicalMessageType technicalMessageType;

    /**
     * The direction the message type can be handled, i.e. SEND, RECEIVE, SEND_RECEIVE.
     */
    @Enumerated(EnumType.STRING)
    private Direction direction;

}
