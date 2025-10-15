package de.agrirouter.middleware.persistence.mongo;

import de.agrirouter.middleware.domain.documents.Notification;
import de.agrirouter.middleware.domain.enums.ChangeType;
import de.agrirouter.middleware.domain.enums.EntityType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository to access the notifications.
 */
public interface NotificationRepository extends MongoRepository<Notification, String> {

    /**
     * Find all notifications for the given external endpoint ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @return The notifications.
     */
    List<Notification> findAllByExternalEndpointId(String externalEndpointId);

    /**
     * Find all notifications for the given external endpoint ID and entity type.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param entityType         The entity type.
     * @return The notifications.
     */
    List<Notification> findAllByExternalEndpointIdAndEntityType(String externalEndpointId, EntityType entityType);

    /**
     * Find all notifications for the given external endpoint ID, entity type and change type.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param entityType         The entity type.
     * @param changeType         The change type.
     * @return The notifications.
     */
    List<Notification> findAllByExternalEndpointIdAndEntityTypeAndChangeType(String externalEndpointId, EntityType entityType, ChangeType changeType);

    /**
     * Delete the notification by the external endpoint ID and the notification ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param notificationId     The notification ID.
     */
    void deleteByExternalEndpointIdAndId(String externalEndpointId, String notificationId);

    /**
     * Find a notification by its external endpoint ID and notification ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param notificationId     The notification ID.
     * @return The notification.
     */
    Notification findByExternalEndpointIdAndId(String externalEndpointId, String notificationId);
}
