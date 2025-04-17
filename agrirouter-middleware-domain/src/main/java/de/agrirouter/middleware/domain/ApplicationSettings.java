package de.agrirouter.middleware.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * Settings for the dedicated application.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class ApplicationSettings extends BaseEntity {

    /**
     * The DDIs to subscribe for. If empty, the default range of DDIs will be subscribed.
     */
    @OneToMany
    @JoinColumn(name = "application_settings_id")
    private Set<DdiCombinationToSubscribeFor> ddiCombinationsToSubscribeFor;

    /**
     * The redirect URL for the onboard process.
     */
    @Column
    private String redirectUrl;

    /**
     * The dedicated router device for MQTT connection handling.
     */
    @OneToOne(cascade = CascadeType.ALL)
    @ToString.Exclude
    private RouterDevice routerDevice;

}
