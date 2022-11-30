package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.persistence.Entity;

/**
 * The current state of the connection.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class ConnectionState extends BaseEntity {

    /**
     * Is the connection available and cached?
     */
    private boolean cached;

    /**
     * Is the connection still connected to the AR?
     */
    private boolean connected;

    /**
     * The client ID that is used for the connection.
     */
    private String clientId;

}
