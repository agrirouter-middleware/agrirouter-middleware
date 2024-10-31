package de.agrirouter.middleware.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A request to register a machine.
 */
@Getter
@Setter
@ToString
@Schema(description = "A request to register a machine.")
public class RegisterMachineRequest {

    @Schema(description = "Your custom team set context ID, in case you want to define it by your own. If the field is empty, a unique ID will be generated.")
    private String customTeamSetContextId;

    /**
     * The device description itself, should be Base64 encoded.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The device description itself, should be Base64 encoded.")
    private String base64EncodedDeviceDescription;

}
