package de.agrirouter.middleware.persistence.mongo;

import de.agrirouter.middleware.domain.documents.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository to access the customers.
 */
@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {

    /**
     * Find all customers for the given external endpoint ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @return The customers.
     */
    List<Customer> findByExternalEndpointId(String externalEndpointId);

    /**
     * Find a customer by its external endpoint ID and customer ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param customerId         The customer ID.
     * @return The customer.
     */
    Optional<Customer> findByExternalEndpointIdAndCustomerId(String externalEndpointId, String customerId);
}
