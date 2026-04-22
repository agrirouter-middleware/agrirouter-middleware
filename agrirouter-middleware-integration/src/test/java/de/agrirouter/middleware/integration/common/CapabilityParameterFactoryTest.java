package de.agrirouter.middleware.integration.common;

import agrirouter.request.payload.endpoint.Capabilities;
import com.dke.data.agrirouter.api.service.parameters.SetCapabilitiesParameters;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.SupportedTechnicalMessageType;
import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CapabilityParameterFactoryTest {

    @Test
    void create_withEmptySupportedMessageTypes_returnsEmptyList() {
        var application = new Application();
        application.setSupportedTechnicalMessageTypes(Set.of());

        var result = CapabilityParameterFactory.create(application);

        assertThat(result).isEmpty();
    }

    @Test
    void create_withSingleSendMessageType_returnsOneCapability() {
        var msgType = new SupportedTechnicalMessageType();
        msgType.setTechnicalMessageType(TemporaryContentMessageType.GPS_INFO);
        msgType.setDirection(Capabilities.CapabilitySpecification.Direction.SEND);

        var application = new Application();
        application.setSupportedTechnicalMessageTypes(Set.of(msgType));

        var result = CapabilityParameterFactory.create(application);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTechnicalMessageType()).isEqualTo(TemporaryContentMessageType.GPS_INFO);
        assertThat(result.get(0).getDirection()).isEqualTo(Capabilities.CapabilitySpecification.Direction.SEND);
    }

    @Test
    void create_withReceiveMessageType_returnsCorrectDirection() {
        var msgType = new SupportedTechnicalMessageType();
        msgType.setTechnicalMessageType(TemporaryContentMessageType.ISO_11783_TIME_LOG);
        msgType.setDirection(Capabilities.CapabilitySpecification.Direction.RECEIVE);

        var application = new Application();
        application.setSupportedTechnicalMessageTypes(Set.of(msgType));

        var result = CapabilityParameterFactory.create(application);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDirection()).isEqualTo(Capabilities.CapabilitySpecification.Direction.RECEIVE);
    }

    @Test
    void create_withSendReceiveMessageType_returnsCorrectDirection() {
        var msgType = new SupportedTechnicalMessageType();
        msgType.setTechnicalMessageType(TemporaryContentMessageType.ISO_11783_TASKDATA_ZIP);
        msgType.setDirection(Capabilities.CapabilitySpecification.Direction.SEND_RECEIVE);

        var application = new Application();
        application.setSupportedTechnicalMessageTypes(Set.of(msgType));

        var result = CapabilityParameterFactory.create(application);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDirection()).isEqualTo(Capabilities.CapabilitySpecification.Direction.SEND_RECEIVE);
    }

    @Test
    void create_withMultipleSupportedTypes_returnsAllCapabilities() {
        var msgType1 = new SupportedTechnicalMessageType();
        msgType1.setTechnicalMessageType(TemporaryContentMessageType.GPS_INFO);
        msgType1.setDirection(Capabilities.CapabilitySpecification.Direction.SEND);

        var msgType2 = new SupportedTechnicalMessageType();
        msgType2.setTechnicalMessageType(TemporaryContentMessageType.ISO_11783_TIME_LOG);
        msgType2.setDirection(Capabilities.CapabilitySpecification.Direction.RECEIVE);

        var msgType3 = new SupportedTechnicalMessageType();
        msgType3.setTechnicalMessageType(TemporaryContentMessageType.DOC_PDF);
        msgType3.setDirection(Capabilities.CapabilitySpecification.Direction.SEND_RECEIVE);

        var application = new Application();
        application.setSupportedTechnicalMessageTypes(Set.of(msgType1, msgType2, msgType3));

        var result = CapabilityParameterFactory.create(application);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(SetCapabilitiesParameters.CapabilityParameters::getTechnicalMessageType)
                .containsExactlyInAnyOrder(
                        TemporaryContentMessageType.GPS_INFO,
                        TemporaryContentMessageType.ISO_11783_TIME_LOG,
                        TemporaryContentMessageType.DOC_PDF
                );
    }
}
