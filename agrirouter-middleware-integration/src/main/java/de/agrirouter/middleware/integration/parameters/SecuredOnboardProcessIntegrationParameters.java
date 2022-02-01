package de.agrirouter.middleware.integration.parameters;

import de.agrirouter.middleware.domain.Application;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Paranmeter class.
 */
@Getter
@Setter
@ToString
public class SecuredOnboardProcessIntegrationParameters {

    /**
     * The ID of the application.
     */
    private String applicationId;

    /**
     * The version of the application.
     */
    private String versionId;

    /**
     * The ID of the endpoint.
     */
    private String externalEndpointId;

    /**
     * The registration code used for the request.
     */
    private String registrationCode;

    /**
     * The private key of the application.
     */
    private String privateKey;

    /**
     * The public key of the application.
     */
    private String publicKey;

}
