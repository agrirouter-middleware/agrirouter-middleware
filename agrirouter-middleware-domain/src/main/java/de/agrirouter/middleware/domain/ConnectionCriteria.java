package de.agrirouter.middleware.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import com.dke.data.agrirouter.api.dto.onboard.RouterDevice;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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

    /**
     * Converts the current instance of ConnectionCriteria to a RouterDevice.ConnectionCriteria object.
     * <p>
     * This method extracts the necessary data from the current instance, such as the client ID, host, and port,
     * and sets these values into a new RouterDevice.ConnectionCriteria object. The port value is parsed from a string
     * to an integer before being set.
     *
     * @return A new instance of RouterDevice.ConnectionCriteria populated with the data from the current instance.
     */
    public RouterDevice.ConnectionCriteria asAgrirouterConnectionCriteria() {
        var agrirouterConnectionCriteria = new RouterDevice.ConnectionCriteria();
        agrirouterConnectionCriteria.setClientId(this.clientId);
        agrirouterConnectionCriteria.setHost(this.host);
        try {
            agrirouterConnectionCriteria.setPort(Integer.parseInt(this.port));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port value: " + this.port, e);
        }
        return agrirouterConnectionCriteria;
    }
}
