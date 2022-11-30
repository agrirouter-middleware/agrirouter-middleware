package de.agrirouter.middleware.domain.log;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.persistence.Entity;

/**
 * An error.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class Error extends LogEntry {
}
