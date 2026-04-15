package de.agrirouter.middleware.business.cache;

import de.agrirouter.middleware.business.global.OnboardStateContainer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OnboardStateContainerTest {

    private final OnboardStateContainer container = new OnboardStateContainer();

    @Test
    void push_returnsNonBlankState() {
        var state = container.push("app-1", "endpoint-1", "tenant-1", "http://redirect.example.com");

        assertThat(state).isNotBlank();
        assertThat(state).hasSize(32);
    }

    @Test
    void push_andPop_returnsCorrectState() {
        var redirectUrl = "http://redirect.example.com/callback";
        var state = container.push("app-123", "ep-456", "tenant-789", redirectUrl);

        var result = container.pop(state);

        assertThat(result).isPresent();
        assertThat(result.get().internalApplicationId()).isEqualTo("app-123");
        assertThat(result.get().externalEndpointId()).isEqualTo("ep-456");
        assertThat(result.get().tenantId()).isEqualTo("tenant-789");
        assertThat(result.get().redirectUrlAfterCallback()).isEqualTo(redirectUrl);
    }

    @Test
    void pop_afterPop_returnsEmptyForSameState() {
        var state = container.push("app-1", "endpoint-1", "tenant-1", "http://redirect.example.com");
        container.pop(state);

        var result = container.pop(state);

        assertThat(result).isEmpty();
    }

    @Test
    void pop_withUnknownState_returnsEmpty() {
        var result = container.pop("unknown-state-xyz");

        assertThat(result).isEmpty();
    }

    @Test
    void push_multipleTimes_generatesUniqueStates() {
        var state1 = container.push("app-1", "ep-1", "tenant-1", "http://url1.example.com");
        var state2 = container.push("app-2", "ep-2", "tenant-2", "http://url2.example.com");

        assertThat(state1).isNotEqualTo(state2);
    }

    @Test
    void push_andPop_removesFromContainer() {
        var state = container.push("app-1", "ep-1", "tenant-1", "http://url.example.com");

        container.pop(state);

        assertThat(container.pop(state)).isEmpty();
    }
}
