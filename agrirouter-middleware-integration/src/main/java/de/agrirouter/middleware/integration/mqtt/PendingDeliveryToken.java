package de.agrirouter.middleware.integration.mqtt;

import lombok.Value;

/**
 * The pending delivery token.
 */
@Value
public class PendingDeliveryToken {

    /**
     * The message ID.
     */
    int messageId;

    /**
     * The granted QoS.
     */
    int[] grantedQos;

    /**
     * The topics.
     */
    String[] topics;

    /**
     * Is the token already completed?
     */
    boolean complete;

    /**
     * The QoS.
     */
    int messageQos;


}
