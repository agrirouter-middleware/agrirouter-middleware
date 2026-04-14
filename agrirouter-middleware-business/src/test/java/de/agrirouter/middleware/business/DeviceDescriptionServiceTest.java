package de.agrirouter.middleware.business;

import de.agrirouter.middleware.domain.documents.Device;
import de.agrirouter.middleware.domain.documents.DeviceDescription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DeviceDescriptionServiceTest {

    @InjectMocks
    private DeviceDescriptionService deviceDescriptionService;

    @Test
    void givenDeviceWithTooManyDescriptionsWhenPruningThenOnlyLatestShouldRemain() {
        // Given
        ReflectionTestUtils.setField(deviceDescriptionService, "deviceDescriptionThreshold", 3);
        Device device = new Device();
        List<DeviceDescription> deviceDescriptions = new ArrayList<>();
        deviceDescriptions.add(createDeviceDescription("1"));
        deviceDescriptions.add(createDeviceDescription("2"));
        deviceDescriptions.add(createDeviceDescription("3"));
        deviceDescriptions.add(createDeviceDescription("4"));
        deviceDescriptions.add(createDeviceDescription("5"));
        device.setDeviceDescriptions(deviceDescriptions);

        // When
        ReflectionTestUtils.invokeMethod(deviceDescriptionService, "pruneDeviceDescriptions", device);

        // Then
        assertThat(device.getDeviceDescriptions()).hasSize(3);
        assertThat(device.getDeviceDescriptions().get(0).getExternalEndpointId()).isEqualTo("3");
        assertThat(device.getDeviceDescriptions().get(1).getExternalEndpointId()).isEqualTo("4");
        assertThat(device.getDeviceDescriptions().get(2).getExternalEndpointId()).isEqualTo("5");
    }

    @Test
    void givenDeviceWithNotTooManyDescriptionsWhenPruningThenNothingShouldChange() {
        // Given
        ReflectionTestUtils.setField(deviceDescriptionService, "deviceDescriptionThreshold", 10);
        Device device = new Device();
        List<DeviceDescription> deviceDescriptions = new ArrayList<>();
        deviceDescriptions.add(createDeviceDescription("1"));
        deviceDescriptions.add(createDeviceDescription("2"));
        deviceDescriptions.add(createDeviceDescription("3"));
        device.setDeviceDescriptions(deviceDescriptions);

        // When
        ReflectionTestUtils.invokeMethod(deviceDescriptionService, "pruneDeviceDescriptions", device);

        // Then
        assertThat(device.getDeviceDescriptions()).hasSize(3);
        assertThat(device.getDeviceDescriptions().get(0).getExternalEndpointId()).isEqualTo("1");
        assertThat(device.getDeviceDescriptions().get(1).getExternalEndpointId()).isEqualTo("2");
        assertThat(device.getDeviceDescriptions().get(2).getExternalEndpointId()).isEqualTo("3");
    }

    private DeviceDescription createDeviceDescription(String id) {
        DeviceDescription deviceDescription = new DeviceDescription();
        deviceDescription.setExternalEndpointId(id);
        return deviceDescription;
    }
}
