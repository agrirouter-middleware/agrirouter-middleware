package de.agrirouter.middleware.integration.mqtt;

/**
 * The pending delivery token.
 *
 * @param messageId  The message ID.
 * @param grantedQos The granted QoS.
 * @param topics     The topics.
 * @param complete   Is the token already completed?
 * @param messageQos The QoS.
 */
public record PendingDeliveryToken(int messageId, int[] grantedQos, String[] topics, boolean complete, int messageQos) {

}
