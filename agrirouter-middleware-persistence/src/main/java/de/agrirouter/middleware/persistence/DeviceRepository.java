package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.Device;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Access the devices saved within the database.
 */
public interface DeviceRepository extends MongoRepository<Device, String> {

    /**
     * Find an existing machine.
     *
     * @param manufacturerCode -
     * @param serialNumber     -
     * @return -
     */
    Optional<Device> findByClientName_ManufacturerCodeAndSerialNumber(int manufacturerCode, String serialNumber);

    /**
     * Find all devices for the given internal endpoint id.
     *
     * @param externalEndpointId -
     * @return -
     */
    List<Device> findAllByExternalEndpointId(String externalEndpointId);

    /**
     * Find all devices for the given internal endpoint id where the internal device id is in the list of device IDs.
     *
     * @param externalEndpointId -
     * @param internalDeviceIds  -
     * @return -
     */
    List<Device> findAllByExternalEndpointIdAndInternalDeviceIdIsIn(String externalEndpointId, List<String> internalDeviceIds);

}
