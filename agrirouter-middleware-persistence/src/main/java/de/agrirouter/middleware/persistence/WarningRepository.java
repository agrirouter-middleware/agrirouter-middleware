package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.log.Warning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Access all warnings.
 */
@Repository
public interface WarningRepository extends JpaRepository<Warning, Long> {

    /**
     * Find all warnings for the endpoint.
     *
     * @param endpoint -
     * @return -
     */
    List<Warning> findByEndpoint(Endpoint endpoint);

    /**
     * Remove all warnings for the endpoint.
     *
     * @param endpoint -
     */
    void deleteAllByEndpoint(Endpoint endpoint);

}
