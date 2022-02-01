package de.agrirouter.middleware.domain;

import de.saschadoemer.iso11783.ClientName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Telemetry data, stored in the document storage.
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class Device extends NoSqlBaseEntity {

    /**
     * The serial number.
     */
    private String serialNumber;

    /**
     * The internal ID.
     */
    private String internalDeviceId;

    /**
     * The client name for a device description.
     */
    private ClientName clientName;

    /**
     * The ID of the endpoint.
     */
    private String agrirouterEndpointId;

    /**
     * The external ID of the endpoint.
     */
    private String externalEndpointId;

    /**
     * The device description.
     */
    private List<DeviceDescription> deviceDescriptions = new ArrayList<>();

}
