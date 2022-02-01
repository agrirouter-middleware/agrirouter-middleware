package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.TenantDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * Response class for better API design.
 */
@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "The response when searching for tenants.")
public class FindTenantResponse extends Response {

    /**
     * The tenants.
     */
    @Schema(description = "The tenants found for the request.")
    List<TenantDto> tenants;

}
