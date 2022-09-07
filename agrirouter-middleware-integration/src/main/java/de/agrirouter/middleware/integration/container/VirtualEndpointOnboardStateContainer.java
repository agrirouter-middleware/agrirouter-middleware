package de.agrirouter.middleware.integration.container;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A component holding the onboard states for the virtual endpoint onboard process.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class VirtualEndpointOnboardStateContainer {

    private static final ConcurrentHashMap<String, OnboardState> states = new ConcurrentHashMap<>();

    /**
     * Create a state that can be used after the ACK from the AR has been received.
     *
     * @param messageId          -
     * @param externalEndpointId -
     */
    public void push(String messageId, String externalEndpointId) {
        states.put(messageId, new OnboardState(externalEndpointId));
    }

    public Optional<OnboardState> pop(String messageId) {
        return Optional.ofNullable(states.get(messageId));
    }

    /**
         * The state for the onboard process.
         */
        public record OnboardState(String externalEndpointId) {

    }

}
