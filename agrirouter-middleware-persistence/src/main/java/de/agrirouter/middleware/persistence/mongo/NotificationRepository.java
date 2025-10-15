package de.agrirouter.middleware.persistence.mongo;

import de.agrirouter.middleware.domain.documents.Notification;
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
    List<Notification> findAllByExternalEndpointIdAndEntityTypeAndChangeTypeAndChangeType(String externalEndpointId, EntityType entityType, String changeType);
}
