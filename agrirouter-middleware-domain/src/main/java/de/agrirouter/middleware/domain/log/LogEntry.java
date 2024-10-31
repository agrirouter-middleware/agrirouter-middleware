package de.agrirouter.middleware.domain.log;

import de.agrirouter.middleware.domain.BaseEntity;
import de.agrirouter.middleware.domain.Endpoint;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * An error.
 */
@Data
@ToString
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
abstract class LogEntry extends BaseEntity {

    /**
     * The response code.
     */
    @Column(nullable = false)
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

    /**
     * The belonging endpoint.
     */
    @ManyToOne
    @JoinColumn(name = "endpoint_id")
    private Endpoint endpoint;
}
