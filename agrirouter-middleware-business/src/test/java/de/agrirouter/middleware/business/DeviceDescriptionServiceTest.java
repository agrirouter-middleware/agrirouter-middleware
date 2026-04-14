package de.agrirouter.middleware.business;

import de.agrirouter.middleware.domain.documents.Device;
import de.agrirouter.middleware.domain.documents.DeviceDescription;
import de.agrirouter.middleware.persistence.mongo.DeviceDescriptionRepository;
import de.agrirouter.middleware.persistence.mongo.DeviceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceDescriptionServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceDescriptionRepository deviceDescriptionRepository;

    @InjectMocks
    private DeviceDescriptionService deviceDescriptionService;

    @Test
    void givenDevicesWithTooManyDescriptionsWhenPruningAllThenOnlyLatestShouldRemain() {
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

        when(deviceRepository.findAll()).thenReturn(List.of(device));
        when(deviceDescriptionRepository.findAll()).thenReturn(List.of());

        // When
        deviceDescriptionService.pruneAll();

        // Then
        assertThat(device.getDeviceDescriptions()).hasSize(3);
        assertThat(device.getDeviceDescriptions().get(0).getExternalEndpointId()).isEqualTo("3");
        assertThat(device.getDeviceDescriptions().get(1).getExternalEndpointId()).isEqualTo("4");
        assertThat(device.getDeviceDescriptions().get(2).getExternalEndpointId()).isEqualTo("5");
        verify(deviceRepository, times(1)).save(any());
    }

    @Test
    void givenDeviceDescriptionCollectionWithTooManyDescriptionsWhenPruningAllThenOnlyLatestShouldRemainInCollection() {
        // Given
        ReflectionTestUtils.setField(deviceDescriptionService, "deviceDescriptionThreshold", 3);
        when(deviceRepository.findAll()).thenReturn(List.of());

        DeviceDescription dd1 = createDeviceDescription("1", 100);
        DeviceDescription dd2 = createDeviceDescription("2", 200);
        DeviceDescription dd3 = createDeviceDescription("3", 300);

        when(deviceDescriptionRepository.findAll()).thenReturn(List.of(dd1, dd2, dd3));

        // When
        deviceDescriptionService.pruneAll();

        // Then
        verify(deviceDescriptionRepository, times(1)).deleteAll(argThat(iterable -> {
            List<DeviceDescription> list = new ArrayList<>();
            iterable.forEach(list::add);
            return list.size() == 2 && list.contains(dd1) && list.contains(dd2);
        }));
    }

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

    private DeviceDescription createDeviceDescription(String id, long timestamp) {
        DeviceDescription deviceDescription = new DeviceDescription();
        deviceDescription.setExternalEndpointId(id);
        deviceDescription.setTeamSetContextId("teamSet1");
        deviceDescription.setTimestamp(timestamp);
        return deviceDescription;
    }
}
