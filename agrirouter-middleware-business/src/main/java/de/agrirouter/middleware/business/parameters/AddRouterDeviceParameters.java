package de.agrirouter.middleware.business.parameters;

import com.dke.data.agrirouter.api.enums.CertificationType;
import com.dke.data.agrirouter.api.enums.Gateway;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Parameter class.
 */
@Getter
@Setter
@ToString
public class AddRouterDeviceParameters {

    /**
     * The internal ID of the application within the middleware.
     */
    private String internalApplicationId;

    /**
     * The internal ID of the tenant within the middleware.
     */
    private String tenantId;

    /**
     * The type of the certificate.
     */
    private CertificationType type;

    /**
     * The secret for the certificate.
     */
    private String secret;

    /**
     * The certificate.
     */
    private String certificate;

    /**
     * The client ID.
     */
    private String clientId;

    /**
     * The gateway type.
     */
    private Gateway gateway;

    /**
     * The host.
     */
    private String host;

    /**
     * The port.
     */
    private String port;

    /**
     * The device alternate ID.
     */
    private String deviceAlternateId;

}
