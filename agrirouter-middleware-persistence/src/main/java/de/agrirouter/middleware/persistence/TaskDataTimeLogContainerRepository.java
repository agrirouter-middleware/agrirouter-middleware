package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.taskdata.TaskDataTimeLogContainer;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository to access the time logs within the MongoDB.
 */
public interface TaskDataTimeLogContainerRepository extends MongoRepository<TaskDataTimeLogContainer, String> {
}
