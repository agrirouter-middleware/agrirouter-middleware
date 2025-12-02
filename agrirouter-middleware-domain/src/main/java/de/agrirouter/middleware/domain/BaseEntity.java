package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base entity, holding common attributes like the technical ID, etc.
 */
@Data
@ToString
public abstract class BaseEntity implements Serializable {

    /**
     * Technical ID of the entity.
     */
    @Id
    private String id;

    /**
     * The last update.
     */
    private LocalDateTime lastUpdate;

    /**
     * Define timestamp for the last update.
     */
    public void setLastUpdate() {
        lastUpdate = LocalDateTime.now();
    }

}
