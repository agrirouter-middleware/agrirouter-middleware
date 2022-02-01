package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.EndpointStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Access the endpoint status.
 */
public interface EndpointStatusRepository extends JpaRepository<EndpointStatus, Long> {
}
