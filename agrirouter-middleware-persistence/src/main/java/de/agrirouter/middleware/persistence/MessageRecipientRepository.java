package de.agrirouter.middleware.persistence;

import de.agrirouter.middleware.domain.MessageRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Access the message recipients.
 */
public interface MessageRecipientRepository extends JpaRepository<MessageRecipient, Long> {
}
