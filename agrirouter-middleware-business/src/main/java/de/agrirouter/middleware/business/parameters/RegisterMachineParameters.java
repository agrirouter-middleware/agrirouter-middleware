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

    /**
     * The custom team set context ID, in case you want to define it by your own. If the field is empty, a unique ID will be generated.
     */
    private String customTeamSetContextId;


}
