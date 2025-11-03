package de.agrirouter.middleware.domain.log;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A warning.
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class Warning extends LogEntry {
}
