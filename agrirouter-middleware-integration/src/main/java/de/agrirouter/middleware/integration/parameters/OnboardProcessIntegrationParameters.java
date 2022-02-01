package de.agrirouter.middleware.integration.parameters;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Paranmeter class.
 */
@Getter
@Setter
@ToString
public class OnboardProcessIntegrationParameters {

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
    private String endpointId;

    /**
     * The registration code used for the request.
     */
    private String registrationCode;

}
