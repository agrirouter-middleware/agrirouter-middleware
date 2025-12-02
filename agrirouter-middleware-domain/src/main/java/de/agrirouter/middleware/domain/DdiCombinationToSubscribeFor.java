package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A combination of DDIs to subscribe for.
 */
@Data
@Document
@ToString
@EqualsAndHashCode(callSuper = true)
public class DdiCombinationToSubscribeFor extends BaseEntity {

    /**
     * Beginning of the range.
     */
    private int start;

    /**
     * End of the range.
     */
    private int end;

}
