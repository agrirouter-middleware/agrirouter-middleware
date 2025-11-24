package de.agrirouter.middleware.domain;

import agrirouter.request.payload.endpoint.Capabilities.CapabilitySpecification.Direction;
import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A supported technical message type.
 * Every technical message type has a direction and a agrirouter© specific technical message type.
 */
@Data
@Document
@ToString
@EqualsAndHashCode(callSuper = true)
public class SupportedTechnicalMessageType extends BaseEntity {

    /**
     * The technical message type, that the application does support, i.e. TaskData, EFDI, etc.
     */
    private TemporaryContentMessageType technicalMessageType;

    /**
     * The direction the message type can be handled, i.e. SEND, RECEIVE, SEND_RECEIVE.
     */
    private Direction direction;

}
