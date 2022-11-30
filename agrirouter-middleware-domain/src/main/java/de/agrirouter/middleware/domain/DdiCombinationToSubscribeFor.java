package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.persistence.Entity;

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
