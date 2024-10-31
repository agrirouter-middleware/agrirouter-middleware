package de.agrirouter.middleware.domain.log;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * An error.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class Error extends LogEntry {
}
