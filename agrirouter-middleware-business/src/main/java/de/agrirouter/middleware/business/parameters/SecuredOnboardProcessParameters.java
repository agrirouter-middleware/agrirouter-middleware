package de.agrirouter.middleware.business.parameters;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Paranmeter class.
 */
@Getter
@Setter
@ToString
public class SecuredOnboardProcessParameters {

    /**
     * The internal ID of the application within the middleware.
     */
    private String internalApplicationId;

    /**
     * The internal ID of the tenant within the middleware.
     */
    private String tenantId;

    /**
     * The ID of the endpoint.
     */
    private String endpointId;

    /**
     * The registration code used for the request.
     */
    private String registrationCode;

}
