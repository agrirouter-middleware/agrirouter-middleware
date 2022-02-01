package de.agrirouter.middleware.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * A request to register a machine.
 */
@Getter
@Setter
@ToString
@Schema(description = "A request to register a machine.")
public class RegisterMachineRequest {

    /**
     * The device description itself, should be Base64 encoded.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The device description itself, should be Base64 encoded.")
    private String base64EncodedDeviceDescription;

}
