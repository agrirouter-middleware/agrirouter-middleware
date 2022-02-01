package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

/**
 * DTO for listing all tenants.
 */
@Data
@ToString
@Schema(description = "Representation of a tenant within the middleware.")
public class TenantDto {

    /**
     * Internal ID of the tenant.
     */
    @Schema(description = "Internal ID of the tenant.")
    private String tenantId;

    /**
     * Name of the tenant.
     */
    @Schema(description = "Name of the tenant.")
    private String name;

}
