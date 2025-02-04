package de.agrirouter.middleware.persistence.mongo;

import de.agrirouter.middleware.domain.documents.TaskDataTimeLogContainer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to access the time logs within the MongoDB.
 */
@Repository
public interface TaskDataTimeLogContainerRepository extends MongoRepository<TaskDataTimeLogContainer, String> {
}
