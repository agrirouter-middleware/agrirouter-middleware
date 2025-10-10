package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * A container holding the basic representation of a field.
 */
@Getter
@Setter
@Schema(description = "A container holding the basic representation of a field (the EFDI representation).")
public class FieldDto {

    @Schema(description = "The field as JSON.")
    private String fieldAsJson;

}
