package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * Connection details for a router device.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class ConnectionCriteria extends BaseEntity {

    /**
     * The client ID.
     */
    @Column(nullable = false)
    private String clientId;

    /**
     * The host.
     */
    @Column(nullable = false)
    private String host;

    /**
     * The port.
     */
    @Column(nullable = false)
    private String port;

}
