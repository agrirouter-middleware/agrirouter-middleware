package de.agrirouter.middleware.domain.log;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Base class for log entries used for mapping to DTOs only. No persistence.
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
abstract class LogEntry {

    /**
     * The response code.
     */
    private int responseCode;

    /**
     * The message of the error.
     */
    private String message;

    /**
     * The timestamp
     */
    private long timestamp;

    /**
     * The type of the response.
     */
    private String responseType;

    /**
     * The ID of the message.
     */
    private String messageId;
}
