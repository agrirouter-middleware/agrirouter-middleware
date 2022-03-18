package de.agrirouter.middleware.integration.parameters;

import de.agrirouter.middleware.domain.Endpoint;
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
public class VirtualOffboardProcessIntegrationParameters {

    /**
     * The endpoint.
     */
    private Endpoint endpoint;

    /**
     * The IDs of the endpoints.
     */
    private List<String> endpointIds;

}
