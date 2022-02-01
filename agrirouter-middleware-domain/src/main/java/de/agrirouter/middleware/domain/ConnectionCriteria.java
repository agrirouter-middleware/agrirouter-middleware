package de.agrirouter.middleware.domain;

import com.dke.data.agrirouter.api.enums.Gateway;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

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
