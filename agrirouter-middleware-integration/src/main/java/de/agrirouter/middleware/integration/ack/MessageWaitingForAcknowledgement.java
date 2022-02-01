package de.agrirouter.middleware.integration.ack;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Message waiting for acknowledgement from the AR.
 * Supporting the concept of asynchronous messaging like MQTT.
 */
@Getter
@Setter
@ToString
public class MessageWaitingForAcknowledgement {

    /**
     * Timestamp for this message waiting for ACK.
     */
    private final long created = Instant.now().getEpochSecond();

    /**
     * The internal endpoint ID.
     */
    private String agrirouterEndpointId;

    /**
     * The message ID.
     */
    private String messageId;

    /**
     * The response from the AR - in case of an error.
     */
    private String response;

    /**
     * The type of message waiting for response.
     */
    private String technicalMessageType;

    /**
     * Dynamic properties for the message waiting for ACK.
     */
    private Map<String, Object> dynamicProperties = new HashMap<>();

    /**
     * Get a dynamic property as string.
     *
     * @param key -
     * @return -
     */
    public String getDynamicPropertyAsString(String key) {
        return (String) dynamicProperties.get(key);
    }
}
