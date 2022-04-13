package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base entity, holding common attributes like the technical ID, etc.
 */
@Data
@ToString
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    /**
     * Technical ID of the entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The last update.
     */
    private LocalDateTime lastUpdate;

    /**
     * Define timestamp for the last update.
     */
    @PrePersist
    public void setLastUpdate() {
        lastUpdate = LocalDateTime.now();
    }

}
