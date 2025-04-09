package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * A router device from the AR.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class RouterDevice extends BaseEntity {

    /**
     * The device alternate ID.
     */
    private String deviceAlternateId;

    /**
     * Authentication details.
     */
    @OneToOne(cascade = CascadeType.ALL)
    private Authentication authentication;

    /**
     * Connection criteria.
     */
    @OneToOne(cascade = CascadeType.ALL)
    private ConnectionCriteria connectionCriteria;

    /**
     * Converts the current RouterDevice instance into an instance of com.dke.data.agrirouter.api.dto.onboard.RouterDevice.
     * This method maps the device alternate ID, authentication details, and connection criteria from
     * the current instance to create and populate a new RouterDevice object for use with the Agrirouter API.
     *
     * @return A com.dke.data.agrirouter.api.dto.onboard.RouterDevice object representing
     * the current RouterDevice instance with all necessary fields populated.
     */
    public com.dke.data.agrirouter.api.dto.onboard.RouterDevice asAgrirouterRouterDevice() {
        var agrirouterRouterDevice = new com.dke.data.agrirouter.api.dto.onboard.RouterDevice();
        agrirouterRouterDevice.setDeviceAlternateId(this.deviceAlternateId);
        agrirouterRouterDevice.setAuthentication(this.authentication.asAgrirouterAuthentication());
        agrirouterRouterDevice.setConnectionCriteria(this.connectionCriteria.asAgrirouterConnectionCriteria());
        return agrirouterRouterDevice;
    }

}
