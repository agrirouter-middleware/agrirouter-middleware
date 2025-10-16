package de.agrirouter.middleware.business;

import de.agrirouter.middleware.domain.documents.Notification;
import de.agrirouter.middleware.domain.enums.ChangeType;
import de.agrirouter.middleware.domain.enums.EntityType;
import de.agrirouter.middleware.persistence.mongo.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service to access the notifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Find all notifications for the given external endpoint ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @return The notifications.
     */
    public List<Notification> findAllByExternalEndpointId(String externalEndpointId) {
        return notificationRepository.findAllByExternalEndpointId(externalEndpointId);
    }

    /**
     * Find all notifications for the given external endpoint ID and entity type.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param entityType         The entity type.
     * @return The notifications.
     */
    public List<Notification> findAllByExternalEndpointIdAndEntityType(String externalEndpointId, EntityType entityType) {
        return notificationRepository.findAllByExternalEndpointIdAndEntityType(externalEndpointId, entityType);
    }

    /**
     * Find all notifications for the given external endpoint ID, entity type and change type.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param entityType         The entity type.
     * @param changeType         The change type.
     * @return The notifications.
     */
    public List<Notification> findAllByExternalEndpointIdAndEntityTypeAndChangeType(String externalEndpointId, EntityType entityType, ChangeType changeType) {
        return notificationRepository.findAllByExternalEndpointIdAndEntityTypeAndChangeType(externalEndpointId, entityType, changeType);
    }

    /**
     * Mark the entity as created.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param entityType         The entity type.
     * @param entityId           The ID of the affected entity.
     */
    public void created(String externalEndpointId, EntityType entityType, String entityId) {
        var notification = new Notification();
        notification.setCreatedAt(Instant.now());
        notification.setExternalEndpointId(externalEndpointId);
        notification.setChangeType(ChangeType.CREATED);
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        notificationRepository.save(notification);
        log.debug("Created notification for the entity type {} and the external endpoint ID {}.", entityType, externalEndpointId);
    }

    /**
     * Mark the entity as updated.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param entityType         The entity type.
     * @param entityId           The ID of the affected entity.
     */
    public void updated(String externalEndpointId, EntityType entityType, String entityId) {
        var notification = new Notification();
        notification.setCreatedAt(Instant.now());
        notification.setExternalEndpointId(externalEndpointId);
        notification.setChangeType(ChangeType.UPDATED);
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        notificationRepository.save(notification);
        log.debug("Updated notification for the entity type {} and the external endpoint ID {}.", entityType, externalEndpointId);
    }

    /**
     * Mark the notification as read and therefore remove it from the database.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param notificationId     The ID of the notification.
     */
    public void markAsRead(String externalEndpointId, String notificationId) {
        var notification = notificationRepository.findByExternalEndpointIdAndId(externalEndpointId, notificationId);
        if (notification != null) {
            notificationRepository.deleteByExternalEndpointIdAndId(externalEndpointId, notificationId);
            log.debug("Marked notification with ID {} for external endpoint ID {} as read and removed it from the database.", notificationId, externalEndpointId);
        } else {
            log.warn("Notification with ID {} for external endpoint ID {} not found. Nothing was removed.", notificationId, externalEndpointId);
        }
    }
}
