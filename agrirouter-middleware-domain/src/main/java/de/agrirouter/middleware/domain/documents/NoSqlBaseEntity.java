package de.agrirouter.middleware.domain.documents;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;

/**
 * Base entity for NoSQL entities.
 */
@Data
@ToString
public class NoSqlBaseEntity {

    /**
     * Technical ID.
     */
    @Id
    private String id;

}
