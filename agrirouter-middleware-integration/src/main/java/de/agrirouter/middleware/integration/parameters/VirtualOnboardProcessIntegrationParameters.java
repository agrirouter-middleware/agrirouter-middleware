package de.agrirouter.middleware.integration.parameters;

import de.agrirouter.middleware.domain.Endpoint;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Parameters for the onboard process of a virtual endpoint.
 */
@Getter
@Setter
@ToString
public class VirtualOnboardProcessIntegrationParameters {

    /**
     * The endpoint.
     */
    private Endpoint endpoint;

    /**
     * The name of the virtual endpoint.
     */
    private String endpointName;

    /**
     * The ID of the virtual endpoint.
     */
    private String externalVirtualEndpointId;

}
