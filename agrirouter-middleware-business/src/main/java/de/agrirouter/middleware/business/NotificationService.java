package de.agrirouter.middleware.business;

import de.agrirouter.middleware.domain.documents.Notification;
import de.agrirouter.middleware.domain.enums.ChangeType;
import de.agrirouter.middleware.domain.enums.EntityType;
import de.agrirouter.middleware.persistence.mongo.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to access the notifications.
 */
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
}
