package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

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
