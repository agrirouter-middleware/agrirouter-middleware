package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.log.Information;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Access all information.
 */
public interface InformationRepository extends JpaRepository<Information, Long> {

    /**
     * Remove all information for the endpoint.
     *
     * @param endpoint -
     */
    void deleteAllByEndpoint(Endpoint endpoint);

}
