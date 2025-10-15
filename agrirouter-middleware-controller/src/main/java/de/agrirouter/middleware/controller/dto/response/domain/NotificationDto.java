package de.agrirouter.middleware.controller.dto.response.domain;

import de.agrirouter.middleware.domain.enums.ChangeType;
import de.agrirouter.middleware.domain.enums.EntityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Schema(description = "A container holding the basic representation of a notification.")
public class NotificationDto {

    /**
     * The ID.
     */
    @Schema(description = "The ID of the notification.")
    private String id;

    /**
     * The timestamp of the creation.
     */
    @Schema(description = "The timestamp of the creation.")
    private Instant createdAt;

    /**
     * The ID of the external endpoint.
     */
    @Schema(description = "The ID of the external endpoint.")
    private String externalEndpointId;

    /**
     * The ID of the entity.
     */
    @Schema(description = "The ID of the entity.")
    private String entityId;

    /**
     * The type of the entity.
     */
    @Schema(description = "The type of the entity.")
    private EntityType entityType;

    /**
     * The type of change.
     */
    @Schema(description = "The type of change.")
    private ChangeType changeType;
}
