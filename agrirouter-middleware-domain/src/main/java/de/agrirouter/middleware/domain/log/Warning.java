package de.agrirouter.middleware.domain.log;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A warning.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class Warning extends LogEntry {
}
