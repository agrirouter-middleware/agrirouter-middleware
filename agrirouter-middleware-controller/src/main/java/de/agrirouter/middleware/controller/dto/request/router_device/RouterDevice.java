package de.agrirouter.middleware.controller.dto.request.router_device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.OneToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * A router device from the AR.
 */
@Getter
@Setter
@ToString
@Schema(description = "A router device from the AR.")
public class RouterDevice {

    /**
     * The device alternate ID.
     */
    @NotNull
    @NotEmpty
    @Schema(description = "The device alternate ID.")
    private String deviceAlternateId;

    /**
     * Authentication details.
     */
    @NotNull
    @OneToOne
    @Schema(description = "Authentication details.")
    private Authentication authentication;

    /**
     * Connection criteria.
     */
    @NotNull
    @OneToOne
    @Schema(description = "Connection criteria.")
    private ConnectionCriteria connectionCriteria;

}
