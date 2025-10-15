package de.agrirouter.middleware.controller.secured;

import de.agrirouter.middleware.business.NotificationService;
import de.agrirouter.middleware.controller.SecuredApiController;
import de.agrirouter.middleware.controller.dto.response.NotificationsResponse;
import de.agrirouter.middleware.controller.dto.response.domain.NotificationDto;
import de.agrirouter.middleware.domain.documents.Notification;
import de.agrirouter.middleware.domain.enums.ChangeType;
import de.agrirouter.middleware.domain.enums.EntityType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping(SecuredApiController.API_PREFIX + "/notification")
@Tag(
        name = "notifications",
        description = "Operations to fetch notifications and manage them within the middleware."
)
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Find all notifications for the given external endpoint ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @return The notifications.
     */
    @GetMapping("/{externalEndpointId}")
    @Operation(
            summary = "Find all notifications for the given external endpoint ID.",
            description = "Find all notifications for the given external endpoint ID.",
            operationId = "notification.find-all-by-external-endpoint-id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "All notifications for the given external endpoint ID.",
                            content = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = NotificationsResponse.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "In case of an internal server error."
                    )
            }
    )
    public ResponseEntity<NotificationsResponse> findAllByExternalEndpointId(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId) {
        var notifications = notificationService.findAllByExternalEndpointId(externalEndpointId);
        var dtos = convertToDtoList(notifications);
        return ResponseEntity.ok(new NotificationsResponse(dtos));
    }

    /**
     * Find all notifications for the given external endpoint ID and entity type.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param entityType         The entity type.
     * @return The notifications.
     */
    @GetMapping("/{externalEndpointId}/{entityType}")
    @Operation(
            summary = "Find all notifications for the given external endpoint ID and entity type.",
            description = "This endpoint retrieves all notifications for the given external endpoint ID and entity type.",
            operationId = "notification.find-all-by-external-endpoint-id-and-entity-type",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The notifications for the given external endpoint ID and entity type.",
                            content = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = NotificationDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "In case of an internal server error."
                    )
            }
    )
    public ResponseEntity<NotificationsResponse> findAllByExternalEndpointIdAndEntityType(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId, @Parameter(description = "The entity type.", required = true) @PathVariable EntityType entityType) {
        var notifications = notificationService.findAllByExternalEndpointIdAndEntityType(externalEndpointId, entityType);
        var dtos = convertToDtoList(notifications);
        return ResponseEntity.ok(new NotificationsResponse(dtos));
    }

    /**
     * Find all notifications for the given external endpoint ID and entity type.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param entityType         The entity type.
     * @return The notifications.
     */
    @GetMapping("/{externalEndpointId}/{entityType}/{changeType}")
    @Operation(
            summary = "Find all notifications for the given external endpoint ID, entity type and change type.",
            description = "This endpoint retrieves all notifications for the given external endpoint ID, entity type and change type.",
            operationId = "notification.find-all-by-external-endpoint-id-and-entity-type-and-change-type",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The notifications for the given external endpoint ID, entity type and change type.",
                            content = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = NotificationDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "In case of an internal server error."
                    )
            }
    )
    public ResponseEntity<NotificationsResponse> findAllByExternalEndpointIdAndEntityTypeAndChangeType(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId, @Parameter(description = "The entity type.", required = true) @PathVariable EntityType entityType, @Parameter(description = "The change type.", required = true) @PathVariable ChangeType changeType) {
        var notifications = notificationService.findAllByExternalEndpointIdAndEntityTypeAndChangeType(externalEndpointId, entityType, changeType);
        var dtos = convertToDtoList(notifications);
        return ResponseEntity.ok(new NotificationsResponse(dtos));
    }

    @NotNull
    private static List<NotificationDto> convertToDtoList(List<Notification> notifications) {
        return notifications.stream().map(notification -> {
            var dto = new NotificationDto();
            dto.setId(notification.getId());
            dto.setExternalEndpointId(notification.getExternalEndpointId());
            dto.setCreatedAt(notification.getCreatedAt());
            dto.setChangeType(notification.getChangeType());
            dto.setEntityType(notification.getEntityType());
            dto.setEntityId(notification.getEntityId());
            return dto;
        }).toList();
    }
}
