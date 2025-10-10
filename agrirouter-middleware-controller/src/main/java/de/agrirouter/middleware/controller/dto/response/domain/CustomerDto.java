package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * A container holding the basic representation of a customer.
 */
@Getter
@Setter
@Schema(description = "A container holding the basic representation of a customer (the EFDI representation).")
public class CustomerDto {

    @Schema(description = "The customer as JSON.")
    private String customerAsJson;

}
