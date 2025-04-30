package de.agrirouter.middleware.business;

import de.agrirouter.middleware.business.parameters.SearchMachinesParameters;
import de.agrirouter.middleware.domain.documents.Device;
import de.agrirouter.middleware.persistence.mongo.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to handle business operations round about the devices.
 */
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

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
