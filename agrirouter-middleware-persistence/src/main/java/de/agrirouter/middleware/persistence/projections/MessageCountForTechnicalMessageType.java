package de.agrirouter.middleware.persistence.projections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Projection for the message count for a technical message type.
 */
@Getter
@Setter
@AllArgsConstructor
public class MessageCountForTechnicalMessageType {
    private String senderId;
    private String technicalMessageType;
    private long numberOfMessages;
}
