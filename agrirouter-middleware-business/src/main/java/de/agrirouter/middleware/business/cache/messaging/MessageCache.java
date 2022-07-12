package de.agrirouter.middleware.business.cache.messaging;

import com.dke.data.agrirouter.api.enums.ContentMessageType;

import java.util.List;

/**
 * Cache for messages.
 */
public interface MessageCache {

    /**
     * Save message to the internal cache.
     *
     * @param externalEndpointId          External endpoint ID.
     * @param base64EncodedMessageContent Base64 encoded message content.
     * @param filename                    Name of the file.
     * @param recipients                  List of recipients.
     * @param contentMessageType          Content message type.
     */
    void put(String externalEndpointId, String base64EncodedMessageContent, String filename, List<String> recipients, ContentMessageType contentMessageType);

    /**
     * Send all messages in the cache.
     */
    void sendMessages();

    /**
     * Removing all messages.
     */
    void clear();

}
