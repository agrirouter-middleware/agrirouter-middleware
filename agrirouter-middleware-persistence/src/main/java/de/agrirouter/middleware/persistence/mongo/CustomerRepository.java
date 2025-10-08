package de.agrirouter.middleware.persistence.mongo;

import de.agrirouter.middleware.domain.documents.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to access the customers.
 */
@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
}
