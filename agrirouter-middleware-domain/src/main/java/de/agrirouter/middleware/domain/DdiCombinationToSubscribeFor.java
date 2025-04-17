package de.agrirouter.middleware.domain;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A combination of DDIs to subscribe for.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class DdiCombinationToSubscribeFor extends BaseEntity {

    /**
     * Beginning of the range.
     */
    private int start;

    /**
     * End of the range.
     */
    private int end;

}
