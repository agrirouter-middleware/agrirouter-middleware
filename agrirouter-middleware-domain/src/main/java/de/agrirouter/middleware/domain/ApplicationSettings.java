package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

/**
 * Settings for the dedicated application.
 */
@Data
@Document
@ToString
@EqualsAndHashCode(callSuper = true)
public class ApplicationSettings extends BaseEntity {

    /**
     * The DDIs to subscribe for. If empty, the default range of DDIs will be subscribed.
     */
    private Set<DdiCombinationToSubscribeFor> ddiCombinationsToSubscribeFor;

    /**
     * The redirect URL for the onboard process.
     */
    private String redirectUrl;

    /**
     * The dedicated router device for MQTT connection handling.
     */
    @ToString.Exclude
    private RouterDevice routerDevice;

}
