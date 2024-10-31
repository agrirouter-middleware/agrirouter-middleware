package de.agrirouter.middleware.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A router device from the AR.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class RouterDevice extends BaseEntity {

    /**
     * The device alternate ID.
     */
    private String deviceAlternateId;

    /**
     * Authentication details.
     */
    @OneToOne(cascade = CascadeType.ALL)
    private Authentication authentication;

    /**
     * Connection criteria.
     */
    @OneToOne(cascade = CascadeType.ALL)
    private ConnectionCriteria connectionCriteria;

}
