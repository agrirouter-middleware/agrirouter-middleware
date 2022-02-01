package de.agrirouter.middleware.business;

import de.agrirouter.middleware.business.parameters.SearchMachinesParameters;
import de.agrirouter.middleware.domain.Device;
import de.agrirouter.middleware.persistence.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to handle business operations round about the devices.
 */
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * Search for machines.
     *
     * @param searchMachineParameters -
     */
    public List<Device> search(SearchMachinesParameters searchMachineParameters) {
        if (null != searchMachineParameters.getInternalDeviceIds() && !searchMachineParameters.getInternalDeviceIds().isEmpty()) {
            return deviceRepository.findAllByExternalEndpointIdAndInternalDeviceIdIsIn(searchMachineParameters.getExternalEndpointId(), searchMachineParameters.getInternalDeviceIds());
        } else {
            return deviceRepository.findAllByExternalEndpointId(searchMachineParameters.getExternalEndpointId());
        }
    }

}
