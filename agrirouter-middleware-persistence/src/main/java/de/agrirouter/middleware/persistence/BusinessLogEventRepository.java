package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.log.BusinessLogEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Repository to access the business log events within the database.
 */
@Repository
public interface BusinessLogEventRepository extends JpaRepository<BusinessLogEvent, Long> {

    /**
     * Remove all log events after four weeks to keep the space in the database low.
     *
     * @param fourWeeks -
     */
    @Transactional
    void deleteBusinessLogEventByVersionBefore(LocalDateTime fourWeeks);

    /**
     * Remove all log events for the given endpoint.
     *
     * @param endpoint -
     */
    void deleteAllByEndpoint(Endpoint endpoint);

}
