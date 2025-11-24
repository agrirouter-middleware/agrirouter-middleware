package de.agrirouter.middleware.domain;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.google.gson.Gson;
import de.agrirouter.middleware.domain.enums.ApplicationType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

/**
 * Representing an application in the middleware.
 */
@Data
@Document
@ToString
@EqualsAndHashCode(callSuper = true)
public class Application extends BaseEntity {
    private static final Gson GSON = new Gson();

    /**
     * The name of the application.
     */
    private String name;

    /**
     * The internal id of the application.
     */
    @Indexed(unique = true)
    private String internalApplicationId;

    /**
     * The agrirouter© ID of the application.
     */
    private String applicationId;

    /**
     * The version of the application. Each agrirouter© version creates a new application in the middleware.
     */
    @Indexed(unique = true)
    private String versionId;

    /**
     * The supported technical message types for this version of the application.
     */
    @ToString.Exclude
    private Set<SupportedTechnicalMessageType> supportedTechnicalMessageTypes;

    /**
     * The onboard responses that belong to this application.
     */
    private Set<Endpoint> endpoints;

    /**
     * Settings for the application.
     */
    @ToString.Exclude
    private ApplicationSettings applicationSettings;

    /**
     * The private key of the application.
     */
    @ToString.Exclude
    private String privateKey;

    /**
     * Type of the application
     */
    private ApplicationType applicationType;

    /**
     * The registered tenant ID.
     */
    @ToString.Exclude
    private String tenantId;

    /**
     * The public key of the application.
     */
    private String publicKey;

    /**
     * Checks whether the application uses a router device or not.
     *
     * @return -
     */
    public boolean usesRouterDevice() {
        return null != getApplicationSettings() && null != getApplicationSettings().getRouterDevice();
    }

    /**
     * Create an onboarding response for a router device.
     *
     * @param originalOnboardingResponse -
     * @return -
     */
    public String createOnboardResponseForRouterDevice(OnboardingResponse originalOnboardingResponse) {
        if (usesRouterDevice()) {
            originalOnboardingResponse.getConnectionCriteria().setClientId(getApplicationSettings().getRouterDevice().getConnectionCriteria().getClientId());
            originalOnboardingResponse.getConnectionCriteria().setHost(getApplicationSettings().getRouterDevice().getConnectionCriteria().getHost());
            originalOnboardingResponse.getConnectionCriteria().setPort(getApplicationSettings().getRouterDevice().getConnectionCriteria().getPort());
            originalOnboardingResponse.getAuthentication().setCertificate(getApplicationSettings().getRouterDevice().getAuthentication().getCertificate());
            originalOnboardingResponse.getAuthentication().setSecret(getApplicationSettings().getRouterDevice().getAuthentication().getSecret());
            originalOnboardingResponse.getAuthentication().setType(getApplicationSettings().getRouterDevice().getAuthentication().getType().getKey());
            return GSON.toJson(originalOnboardingResponse);
        } else {
            return null;
        }
    }

}
