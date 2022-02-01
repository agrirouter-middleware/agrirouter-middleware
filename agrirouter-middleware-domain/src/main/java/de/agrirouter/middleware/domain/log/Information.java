package de.agrirouter.middleware.domain.log;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Entity;

/**
 * An information.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class Information extends LogEntry {
}
