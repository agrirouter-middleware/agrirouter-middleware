package de.agrirouter.middleware.domain;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.domain.enums.EndpointType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.List;

/**
 * Container holding the onboard responses as JSON.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class Endpoint extends BaseEntity {

    private static final Gson GSON = new Gson();

    /**
     * The ID of an endpoint.
     */
    @Column(length = 36, nullable = false, unique = true)
    private String agrirouterEndpointId;

    /**
     * The ID of an endpoint.
     */
    @Column(length = 36, nullable = false)
    private String agrirouterAccountId;

    /**
     * The external ID of an endpoint.
     */
    @Column(nullable = false, unique = true)
    private String externalEndpointId;

    /**
     * JSON holding the original onboard response from the agrirouter.
     */
    @Lob
    @Column(nullable = false)
    private String onboardResponse;

    /**
     * JSON holding the onboard response in case the endpoint is using a router device from the agrirouter.
     */
    @Lob
    @Column
    private String onboardResponseForRouterDevice;

    /**
     * The connected virtual endpoints.
     */
    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_endpoint_id")
    @ToString.Exclude
    private List<Endpoint> connectedVirtualEndpoints;

    /**
     * Marks an endpoint as deactivated.
     */
    private boolean deactivated;

    /**
     * The type of endpoint, by default NON_VIRTUAL.
     */
    @Enumerated(EnumType.STRING)
    private EndpointType endpointType = EndpointType.NON_VIRTUAL;

    /**
     * The current endpoint status.
     */
    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true)
    private EndpointStatus endpointStatus;

    /**
     * Deliver the internal JSON as DTO.
     *
     * @return -
     */
    public OnboardingResponse asOnboardingResponse() {
        try {
            if (StringUtils.isNotBlank(onboardResponseForRouterDevice)) {
                return GSON.fromJson(onboardResponseForRouterDevice, OnboardingResponse.class);
            } else {
                return GSON.fromJson(onboardResponse, OnboardingResponse.class);
            }
        } catch (JsonParseException e) {
            throw new BusinessException(ErrorMessageFactory.couldNotParseOnboardResponse(), e);
        }
    }

    /**
     * Deliver the internal JSON as DTO.
     *
     * @return -
     */
    public OnboardingResponse asOnboardingResponse(boolean forceUsingTheOriginalOnboardResponse) {
        try {
            if (forceUsingTheOriginalOnboardResponse) {
                return GSON.fromJson(onboardResponse, OnboardingResponse.class);
            } else {
                return asOnboardingResponse();
            }
        } catch (JsonParseException e) {
            throw new BusinessException(ErrorMessageFactory.couldNotParseOnboardResponse(), e);
        }
    }

    /**
     * Checks whether the endpoint is a healthy at the moment.
     *
     * @return -
     */
    public boolean isHealthy() {
        return !isDeactivated() && getEndpointStatus() != null && getEndpointStatus().getConnectionState().isConnected();
    }
}
