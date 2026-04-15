package de.agrirouter.middleware.integration.common;

import agrirouter.request.payload.endpoint.Capabilities;
import com.dke.data.agrirouter.api.service.parameters.SetSubscriptionParameters;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.ApplicationSettings;
import de.agrirouter.middleware.domain.DdiCombinationToSubscribeFor;
import de.agrirouter.middleware.domain.SupportedTechnicalMessageType;
import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionParameterFactoryTest {

    private SubscriptionParameterFactory factory;

    @BeforeEach
    void setUp() {
        factory = new SubscriptionParameterFactory();
        ReflectionTestUtils.setField(factory, "ddiRangeStart", 0);
        ReflectionTestUtils.setField(factory, "ddiRangeEnd", 600);
    }

    private SupportedTechnicalMessageType messageType(TemporaryContentMessageType type,
                                                       Capabilities.CapabilitySpecification.Direction direction) {
        var smt = new SupportedTechnicalMessageType();
        smt.setTechnicalMessageType(type);
        smt.setDirection(direction);
        return smt;
    }

    @Test
    void create_withNoSupportedTypes_returnsEmptyList() {
        var application = new Application();
        application.setSupportedTechnicalMessageTypes(Set.of());

        var result = factory.create(application);

        assertThat(result).isEmpty();
    }

    @Test
    void create_withSendOnlyType_doesNotAddSubscription() {
        var application = new Application();
        application.setSupportedTechnicalMessageTypes(Set.of(
                messageType(TemporaryContentMessageType.GPS_INFO, Capabilities.CapabilitySpecification.Direction.SEND)
        ));

        var result = factory.create(application);

        assertThat(result).isEmpty();
    }

    @Test
    void create_withReceiveType_addsSubscription() {
        var application = new Application();
        application.setSupportedTechnicalMessageTypes(Set.of(
                messageType(TemporaryContentMessageType.GPS_INFO, Capabilities.CapabilitySpecification.Direction.RECEIVE)
        ));

        var result = factory.create(application);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTechnicalMessageType()).isEqualTo(TemporaryContentMessageType.GPS_INFO);
    }

    @Test
    void create_withSendReceiveType_addsSubscription() {
        var application = new Application();
        application.setSupportedTechnicalMessageTypes(Set.of(
                messageType(TemporaryContentMessageType.DOC_PDF, Capabilities.CapabilitySpecification.Direction.SEND_RECEIVE)
        ));

        var result = factory.create(application);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTechnicalMessageType()).isEqualTo(TemporaryContentMessageType.DOC_PDF);
    }

    @Test
    void create_withTimeLogReceive_noAppSettings_addsFullDdiRange() {
        var application = new Application();
        application.setSupportedTechnicalMessageTypes(Set.of(
                messageType(TemporaryContentMessageType.ISO_11783_TIME_LOG, Capabilities.CapabilitySpecification.Direction.RECEIVE)
        ));
        application.setApplicationSettings(null);

        var result = factory.create(application);

        assertThat(result).hasSize(1);
        var subscription = result.get(0);
        assertThat(subscription.getTechnicalMessageType()).isEqualTo(TemporaryContentMessageType.ISO_11783_TIME_LOG);
        assertThat(subscription.getPosition()).isTrue();
        assertThat(subscription.getDdis()).hasSize(601); // 0 to 600 inclusive
        assertThat(subscription.getDdis()).contains(0, 300, 600);
    }

    @Test
    void create_withTimeLogReceive_emptyDdiCombinations_addsFullDdiRange() {
        var appSettings = new ApplicationSettings();
        appSettings.setDdiCombinationsToSubscribeFor(Set.of());

        var application = new Application();
        application.setApplicationSettings(appSettings);
        application.setSupportedTechnicalMessageTypes(Set.of(
                messageType(TemporaryContentMessageType.ISO_11783_TIME_LOG, Capabilities.CapabilitySpecification.Direction.RECEIVE)
        ));

        var result = factory.create(application);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDdis()).hasSize(601);
    }

    @Test
    void create_withTimeLogReceive_customDdiRange_addsOnlyThatRange() {
        var ddiCombination = new DdiCombinationToSubscribeFor();
        ddiCombination.setStart(100);
        ddiCombination.setEnd(110);

        var appSettings = new ApplicationSettings();
        appSettings.setDdiCombinationsToSubscribeFor(Set.of(ddiCombination));

        var application = new Application();
        application.setApplicationSettings(appSettings);
        application.setSupportedTechnicalMessageTypes(Set.of(
                messageType(TemporaryContentMessageType.ISO_11783_TIME_LOG, Capabilities.CapabilitySpecification.Direction.RECEIVE)
        ));

        var result = factory.create(application);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDdis()).hasSize(11); // 100 to 110 inclusive
        assertThat(result.get(0).getDdis()).contains(100, 105, 110);
        assertThat(result.get(0).getDdis()).doesNotContain(99, 111);
    }

    @Test
    void create_withTimeLogReceive_multipleDdiRanges_combinesRanges() {
        var range1 = new DdiCombinationToSubscribeFor();
        range1.setStart(0);
        range1.setEnd(5);

        var range2 = new DdiCombinationToSubscribeFor();
        range2.setStart(10);
        range2.setEnd(12);

        var appSettings = new ApplicationSettings();
        appSettings.setDdiCombinationsToSubscribeFor(Set.of(range1, range2));

        var application = new Application();
        application.setApplicationSettings(appSettings);
        application.setSupportedTechnicalMessageTypes(Set.of(
                messageType(TemporaryContentMessageType.ISO_11783_TIME_LOG, Capabilities.CapabilitySpecification.Direction.RECEIVE)
        ));

        var result = factory.create(application);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDdis()).hasSize(9); // 0-5 (6 entries) + 10-12 (3 entries)
        assertThat(result.get(0).getDdis()).containsAll(List.of(0, 1, 2, 3, 4, 5, 10, 11, 12));
    }

    @Test
    void create_withNonTimeLogReceive_doesNotSetDdisOrPosition() {
        var application = new Application();
        application.setSupportedTechnicalMessageTypes(Set.of(
                messageType(TemporaryContentMessageType.ISO_11783_TASKDATA_ZIP, Capabilities.CapabilitySpecification.Direction.RECEIVE)
        ));

        var result = factory.create(application);

        assertThat(result).hasSize(1);
        var subscription = result.get(0);
        assertThat(subscription.getDdis()).isNullOrEmpty();
        assertThat(subscription.getPosition()).isFalse();
    }

    @Test
    void create_withMixOfSendAndReceiveTypes_addsOnlyReceiveAndSendReceive() {
        var application = new Application();
        application.setSupportedTechnicalMessageTypes(Set.of(
                messageType(TemporaryContentMessageType.GPS_INFO, Capabilities.CapabilitySpecification.Direction.SEND),
                messageType(TemporaryContentMessageType.DOC_PDF, Capabilities.CapabilitySpecification.Direction.RECEIVE),
                messageType(TemporaryContentMessageType.IMG_JPEG, Capabilities.CapabilitySpecification.Direction.SEND_RECEIVE)
        ));

        var result = factory.create(application);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SetSubscriptionParameters.Subscription::getTechnicalMessageType)
                .containsExactlyInAnyOrder(
                        TemporaryContentMessageType.DOC_PDF,
                        TemporaryContentMessageType.IMG_JPEG
                );
    }
}
