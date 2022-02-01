package de.agrirouter.middleware.business.parameters;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Parameters to search for machines.
 */
@Setter
@Getter
@ToString
public class SearchMachinesParameters {

    /**
     * The external endpoint ID.
     */
    private String externalEndpointId;

    /**
     * The list of internal device IDs to search for.
     */
    private List<String> internalDeviceIds;

}
