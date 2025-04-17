package de.agrirouter.middleware.controller.dto.request.router_device;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Connection details for a router device.
 */
@Getter
@Setter
@ToString
@Schema(description = "Connection details for a router device.")
public class ConnectionCriteria {

    /**
     * The client ID.
     */
    @NotNull
    @NotEmpty
    @Schema(description = "The client ID.")
    private String clientId;

    /**
     * The host.
     */
    @NotNull
    @NotEmpty
    @Schema(description = "The host.")
    private String host;

    /**
     * The port.
     */
    @NotNull
    @NotEmpty
    @Schema(description = "The port.")
    private String port;

}
