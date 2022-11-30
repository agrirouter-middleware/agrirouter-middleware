package de.agrirouter.middleware.domain.log;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.persistence.Entity;

/**
 * A warning.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class Warning extends LogEntry {
}
