package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

/**
 * DTO.
 */
@Data
@ToString
@Schema(description = "A container holding the basic representation of an application.")
public class ApplicationDto {

    /**
     * Technical ID of the entity.
     */
    @Schema(description = "The technical ID of the entity.")
    private long id;

    /**
     * The name of the application.
     */
    @Schema(description = "The name of the application.")
    private String name;

    /**
     * The agrirouter© ID of the application.
     */
    @Schema(description = "The agrirouter© ID of the application.")
    private String applicationId;

    /**
     * The internal ID of the application.
     */
    @Schema(description = "The internal ID of the application.")
    private String internalApplicationId;

    /**
     * The version of the application. Each agrirouter© version creates a new application in the middleware.
     */
    @Schema(description = "The version of the application. Each agrirouter© version creates a new application in the middleware.")
    private String versionId;

}
