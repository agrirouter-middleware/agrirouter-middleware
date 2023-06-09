package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.RouterDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to access the router devices within the database.
 */
@Repository
public interface RouterDeviceRepository extends JpaRepository<RouterDevice, Long> {

    /**
     * Check if a router device exists by the given client ID.
     *
     * @param id       the ID
     * @param clientId the client ID
     * @return true if the router device exists, otherwise false
     */
    boolean existsByIdNotAndConnectionCriteria_ClientId(Long id, String clientId);
}
