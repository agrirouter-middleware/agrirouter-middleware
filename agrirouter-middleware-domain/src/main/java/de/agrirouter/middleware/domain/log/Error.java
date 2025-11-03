package de.agrirouter.middleware.domain.log;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * An error.
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class Error extends LogEntry {
}
