package de.agrirouter.middleware.controller.dto.response.domain;

import de.saschadoemer.iso11783.ClientName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.bson.Document;

/**
 * The device found.
 */
@Data
@ToString
@Schema(description = "A device, stored within the middleware.")
public class DeviceDto {

    /**
     * The serial number.
     */
    @Schema(description = "The serial number.")
    private String serialNumber;

    /**
     * The internal device ID.
     */
    @Schema(description = "The internal device ID.")
    private String internalDeviceId;

    /**
     * The client name for the device.
     */
    @Schema(description = "The client name for the device.")
    private ClientName clientName;

    /**
     * The ID of the endpoint.
     */
    @Schema(description = "The ID of the endpoint.")
    private String agrirouterEndpointId;

    /**
     * The external ID of the endpoint.
     */
    @Schema(description = "The external ID of the endpoint.")
    private String externalEndpointId;

    /**
     * The current device description.
     */
    @Schema(description = "The current device description.")
    private Document currentDeviceDescription;

}
