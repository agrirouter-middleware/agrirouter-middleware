package de.agrirouter.middleware.business.parameters;

import de.agrirouter.middleware.domain.Endpoint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Parameters to create a machine.
 */
@Getter
@Setter
@ToString
public class CreateDeviceDescriptionParameters {

    /**
     * The endpoint.
     */
    private Endpoint endpoint;

    /**
     * The team set context id.
     */
    private String teamSetContextId;

    /**
     * The base64 encoded device description.
     */
    private String base64EncodedDeviceDescription;

}
