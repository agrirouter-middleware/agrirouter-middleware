package de.agrirouter.middleware.business;

import de.agrirouter.middleware.business.parameters.SearchMachinesParameters;
import de.agrirouter.middleware.domain.documents.Device;
import de.agrirouter.middleware.persistence.mongo.DeviceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService deviceService;

    @Test
    void search_withInternalDeviceIds_usesFilteredQuery() {
        var externalEndpointId = "endpoint-123";
        var internalDeviceIds = List.of("device-1", "device-2");
        var expectedDevices = List.of(new Device(), new Device());
        when(deviceRepository.findAllByExternalEndpointIdAndInternalDeviceIdIsIn(externalEndpointId, internalDeviceIds))
                .thenReturn(expectedDevices);

        var params = new SearchMachinesParameters();
        params.setExternalEndpointId(externalEndpointId);
        params.setInternalDeviceIds(internalDeviceIds);

        var result = deviceService.search(params);

        assertThat(result).hasSize(2);
        verify(deviceRepository).findAllByExternalEndpointIdAndInternalDeviceIdIsIn(externalEndpointId, internalDeviceIds);
    }

    @Test
    void search_withoutInternalDeviceIds_usesBroadQuery() {
        var externalEndpointId = "endpoint-456";
        var expectedDevices = List.of(new Device());
        when(deviceRepository.findAllByExternalEndpointId(externalEndpointId)).thenReturn(expectedDevices);

        var params = new SearchMachinesParameters();
        params.setExternalEndpointId(externalEndpointId);

        var result = deviceService.search(params);

        assertThat(result).hasSize(1);
        verify(deviceRepository).findAllByExternalEndpointId(externalEndpointId);
    }

    @Test
    void search_withEmptyInternalDeviceIds_usesBroadQuery() {
        var externalEndpointId = "endpoint-789";
        var expectedDevices = List.of(new Device(), new Device(), new Device());
        when(deviceRepository.findAllByExternalEndpointId(externalEndpointId)).thenReturn(expectedDevices);

        var params = new SearchMachinesParameters();
        params.setExternalEndpointId(externalEndpointId);
        params.setInternalDeviceIds(Collections.emptyList());

        var result = deviceService.search(params);

        assertThat(result).hasSize(3);
        verify(deviceRepository).findAllByExternalEndpointId(externalEndpointId);
    }

    @Test
    void search_withNullInternalDeviceIds_usesBroadQuery() {
        var externalEndpointId = "endpoint-null";
        var expectedDevices = Collections.<Device>emptyList();
        when(deviceRepository.findAllByExternalEndpointId(externalEndpointId)).thenReturn(expectedDevices);

        var params = new SearchMachinesParameters();
        params.setExternalEndpointId(externalEndpointId);
        params.setInternalDeviceIds(null);

        var result = deviceService.search(params);

        assertThat(result).isEmpty();
        verify(deviceRepository).findAllByExternalEndpointId(externalEndpointId);
    }
}
