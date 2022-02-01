package de.agrirouter.middleware.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
