package de.agrirouter.middleware.business.parameters;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Parameters for the offboard process of a virtual endpoint.
 */
@Getter
@Setter
@ToString
public class VirtualOffboardProcessParameters {

    /**
     * The ID of the endpoint.
     */
    private String externalEndpointId;

    /**
     * The details of a virtual endpoint.
     */
    private List<String> externalVirtualEndpointIds;

}
