package de.agrirouter.middleware.business.parameters;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Parameter class.
 */
@Getter
@Setter
@ToString
public class OnboardProcessParameters {

    /**
     * The internal ID of the application within the middleware.
     */
    private String internalApplicationId;

    /**
     * The internal ID of the tenant within the middleware.
     */
    private String tenantId;

    /**
     * The external ID of the endpoint.
     */
    private String externalEndpointId;

    /**
     * The registration code used for the request.
     */
    private String registrationCode;

    /**
     * The account ID of the user.
     */
    private String accountId;

}
