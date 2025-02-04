package de.agrirouter.middleware.persistence.jpa;

import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.log.Error;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Access all errors.
 */
public interface ErrorRepository extends JpaRepository<Error, Long> {

    /**
     * Find all errors for the endpoint.
     *
     * @param endpoint -
     * @return -
     */
    List<Error> findByEndpoint(Endpoint endpoint);

    /**
     * Remove all errors for the endpoint.
     *
     * @param endpoint -
     */
    void deleteAllByEndpoint(Endpoint endpoint);
}
