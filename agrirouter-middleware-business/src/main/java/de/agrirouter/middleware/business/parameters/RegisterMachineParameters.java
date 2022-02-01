package de.agrirouter.middleware.business.parameters;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Parameters to register a new machine.
 */
@Setter
@Getter
@ToString
public class RegisterMachineParameters {

    /**
     * The base64 encoded device description.
     */
    private String base64EncodedDeviceDescription;

    /**
     * The external endpoint ID.
     */
    private String externalEndpointId;

}
