package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO.
 */
@Getter
@Setter
@Schema(description = "The current status of the application incl. its endpoints.")
public class ApplicationWithEndpointStatusDto {

    /**
     * The technical ID of the entity.
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


    /**
     * Marker if the application uses a router device.
     */
    @Schema(description = "Marker if the application uses a router device.")
    private boolean usesRouterDevice;
    
    /**
     * The status information for all endpoints within the application.
     */
    @Schema(description = "The status information for all endpoints within the application.")
    private List<EndpointWithStatusDto> endpointsWithStatus;


}
