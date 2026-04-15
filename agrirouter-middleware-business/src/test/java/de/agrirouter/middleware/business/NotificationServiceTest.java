package de.agrirouter.middleware.business;

import de.agrirouter.middleware.domain.documents.Notification;
import de.agrirouter.middleware.domain.enums.ChangeType;
import de.agrirouter.middleware.domain.enums.EntityType;
import de.agrirouter.middleware.persistence.mongo.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void findAllByExternalEndpointId_delegatesToRepository() {
        var endpointId = "endpoint-123";
        var notifications = List.of(new Notification(), new Notification());
        when(notificationRepository.findAllByExternalEndpointId(endpointId)).thenReturn(notifications);

        var result = notificationService.findAllByExternalEndpointId(endpointId);

        assertThat(result).hasSize(2);
        verify(notificationRepository).findAllByExternalEndpointId(endpointId);
    }

    @Test
    void findAllByExternalEndpointIdAndEntityType_delegatesToRepository() {
        var endpointId = "endpoint-456";
        var entityType = EntityType.FIELD;
        var notifications = List.of(new Notification());
        when(notificationRepository.findAllByExternalEndpointIdAndEntityType(endpointId, entityType)).thenReturn(notifications);

        var result = notificationService.findAllByExternalEndpointIdAndEntityType(endpointId, entityType);

        assertThat(result).hasSize(1);
        verify(notificationRepository).findAllByExternalEndpointIdAndEntityType(endpointId, entityType);
    }

    @Test
    void findAllByExternalEndpointIdAndEntityTypeAndChangeType_delegatesToRepository() {
        var endpointId = "endpoint-789";
        var entityType = EntityType.FARM;
        var changeType = ChangeType.CREATED;
        var notifications = List.of(new Notification(), new Notification(), new Notification());
        when(notificationRepository.findAllByExternalEndpointIdAndEntityTypeAndChangeType(endpointId, entityType, changeType))
                .thenReturn(notifications);

        var result = notificationService.findAllByExternalEndpointIdAndEntityTypeAndChangeType(endpointId, entityType, changeType);

        assertThat(result).hasSize(3);
        verify(notificationRepository).findAllByExternalEndpointIdAndEntityTypeAndChangeType(endpointId, entityType, changeType);
    }

    @Test
    void created_savesNotificationWithCreatedChangeType() {
        var endpointId = "endpoint-abc";
        var entityType = EntityType.CUSTOMER;
        var entityId = "entity-xyz";
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.created(endpointId, entityType, entityId);

        var captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getExternalEndpointId()).isEqualTo(endpointId);
        assertThat(saved.getEntityType()).isEqualTo(entityType);
        assertThat(saved.getEntityId()).isEqualTo(entityId);
        assertThat(saved.getChangeType()).isEqualTo(ChangeType.CREATED);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void updated_savesNotificationWithUpdatedChangeType() {
        var endpointId = "endpoint-def";
        var entityType = EntityType.FIELD;
        var entityId = "entity-updated";
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.updated(endpointId, entityType, entityId);

        var captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getExternalEndpointId()).isEqualTo(endpointId);
        assertThat(saved.getEntityType()).isEqualTo(entityType);
        assertThat(saved.getEntityId()).isEqualTo(entityId);
        assertThat(saved.getChangeType()).isEqualTo(ChangeType.UPDATED);
    }

    @Test
    void markAsRead_withExistingNotification_deletesIt() {
        var endpointId = "endpoint-mark";
        var notificationId = "notif-001";
        var notification = new Notification();
        when(notificationRepository.findByExternalEndpointIdAndId(endpointId, notificationId)).thenReturn(notification);

        notificationService.markAsRead(endpointId, notificationId);

        verify(notificationRepository).deleteByExternalEndpointIdAndId(endpointId, notificationId);
    }

    @Test
    void markAsRead_withNonExistingNotification_doesNothing() {
        var endpointId = "endpoint-ghost";
        var notificationId = "notif-404";
        when(notificationRepository.findByExternalEndpointIdAndId(endpointId, notificationId)).thenReturn(null);

        notificationService.markAsRead(endpointId, notificationId);

        verify(notificationRepository, never()).deleteByExternalEndpointIdAndId(any(), any());
    }
}
