package de.agrirouter.middleware.domain.documents;

import de.saschadoemer.iso11783.clientname.ClientName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Telemetry data, stored in the document storage.
 */
@Data
@ToString
@Document
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
