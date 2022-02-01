package de.agrirouter.middleware.controller.dto.request;

import de.agrirouter.middleware.controller.dto.request.router_device.RouterDevice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * Add a router device to the application.
 */
@Getter
@Setter
@ToString
@Schema(description = "Request to add a router device to the application.")
public class AddRouterDeviceRequest {

    /**
     * The router device.
     */
    @NotNull
    @Schema(description = "The router device.")
    private RouterDevice routerDevice;

}
