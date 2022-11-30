package de.agrirouter.middleware.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

/**
 * The status of an endpoint.
 */
@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
public class EndpointStatus extends BaseEntity {

    /**
     * The number of messages within the inbox.
     */
    private int nrOfMessagesWithinTheInbox;

    /**
     * The state of the connection.
     */
    @OneToOne(cascade = CascadeType.ALL)
    private ConnectionState connectionState;

}
