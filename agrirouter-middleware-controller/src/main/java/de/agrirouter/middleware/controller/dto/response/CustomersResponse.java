package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.CustomerDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Response for all customers for the account.")
public class CustomersResponse extends Response {

    @Schema(description = "The external endpoint id.")
    String externalEndpointId;

    @Schema(description = "The customers for the account.")
    List<CustomerDto> customers;

}
