package de.agrirouter.middleware.persistence.mongo;

import de.agrirouter.middleware.domain.documents.Device;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Access the devices saved within the database.
 */
@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {

    /**
     * Find an existing machine.
     *
     * @param manufacturerCode   -
     * @param serialNumber       -
     * @param externalEndpointId -
     * @return -
     */
    Optional<Device> findByClientName_ManufacturerCodeAndSerialNumberAndExternalEndpointId(int manufacturerCode, String serialNumber, String externalEndpointId);

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
