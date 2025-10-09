package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * A container holding the basic representation of a farm.
 */
@Getter
@Setter
@Schema(description = "A container holding the basic representation of a farm (the EFDI representation.")
public class FarmDto {

    @Schema(description = "The farm as JSON.")
    private String farmAsJson;

}
