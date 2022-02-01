package de.agrirouter.middleware.business.parameters;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Parameters for the onboard process of a virtual endpoint.
 */
@Getter
@Setter
@ToString
public class VirtualOnboardProcessParameters {

    /**
     * The external ID of the endpoint.
     */
    private String externalEndpointId;

    /**
     * The name of the virtual endpoint.
     */
    private String endpointName;

    /**
     * The ID of the virtual endpoint.
     */
    private String externalVirtualEndpointId;

}
