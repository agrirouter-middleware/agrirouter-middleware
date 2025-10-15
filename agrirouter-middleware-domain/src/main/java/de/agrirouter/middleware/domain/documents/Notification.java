package de.agrirouter.middleware.domain.documents;

import de.agrirouter.middleware.domain.enums.ChangeType;
import de.agrirouter.middleware.domain.enums.EntityType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * A notification, stored in the document storage
 */
@Data
@ToString
@Document
@EqualsAndHashCode(callSuper = true)
public class Notification extends NoSqlBaseEntity {

    /**
     * The timestamp of the creation.
     */
    private Instant createdAt;

    /**
     * The ID of the external endpoint.
     */
    private String externalEndpointId;

    /**
     * The ID of the entity.
     */
    private String entityId;

    /**
     * The type of the entity.
     */
    private EntityType entityType;

    /**
     * The type of change.
     */
    private ChangeType changeType;

    /**
     * The timestamp of the notification.
     */
    @Indexed(name = "ttl_index", expireAfter = "${app.notification.ttl}")
    private Instant exiredOn;

}
