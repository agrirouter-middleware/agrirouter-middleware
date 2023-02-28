package de.agrirouter.middleware.business.global;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A component holding the onboard states for the application.
 */
@Component
public class OnboardStateContainer {

    private static final ConcurrentHashMap<String, OnboardState> states = new ConcurrentHashMap<>();

    /**
     * Create a state for the application to return afterwards.
     *
     * @param internalApplicationId The ID of the application.
     * @param externalEndpointId    The endpoint ID.
     * @param tenantId              The ID of the tenant.
     * @param redirectUrl           The redirect URL after callback.
     * @return The ID of the state.
     */
    public String push(String internalApplicationId, String externalEndpointId, String tenantId, String redirectUrl) {
        final var state = RandomStringUtils.randomAlphabetic(32);
        states.put(state, new OnboardState(internalApplicationId, externalEndpointId, tenantId, redirectUrl));
        return state;
    }

    public Optional<OnboardState> pop(String state) {
        final var onboardState = Optional.ofNullable(states.get(state));
        if (onboardState.isPresent()) {
            states.remove(state);
        }
        return onboardState;
    }

    /**
     * The state for the onboard process.
     */
    @Getter
    @AllArgsConstructor
    public static class OnboardState {

        /**
         * The internal application ID.
         */
        private final String internalApplicationId;

        /**
         * The external endpoint ID in case this is the onboard process for an existing endpoint.
         */
        private final String externalEndpointId;

        /**
         * The ID of the tenant.
         */
        private final String tenantId;

        /**
         * The redirect URL after the callback has been called.
         */
        private final String redirectUrlAfterCallback;
    }

}
