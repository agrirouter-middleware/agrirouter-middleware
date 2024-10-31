package de.agrirouter.middleware.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request to register a tenant.
 */
@Getter
@Setter
@ToString
@Schema(description = "Request to register a tenant.")
public class TenantRegistrationRequest {
    /**
     * Name of the tenant, has to be unique.
     */
    @NotNull
    @NotEmpty
    @Schema(description = "Name of the tenant, has to be unique.")
    private String name;


}
