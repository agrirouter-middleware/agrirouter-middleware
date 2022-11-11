package de.agrirouter.middleware.integration.ack;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
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
     * @param key The key of the property.
     * @return The value of the property.
     */
    public String getDynamicPropertyAsString(String key) {
        return (String) dynamicProperties.get(key);
    }

    /**
     * Get a dynamic property as string set.
     *
     * @param key The key of the property.
     * @return The value of the property.
     */
    public List<String> getDynamicPropertyAsStringList(String key) {
        //noinspection unchecked
        return (List<String>) dynamicProperties.get(key);
    }

    /**
     * Check if the message waiting for ACK is older than one week.
     *
     * @return True if the message is older than one week.
     */
    public boolean isOlderThanOneWeek() {
        return created < Instant.now().minusSeconds(60 * 60 * 24 * 7).getEpochSecond();
    }
}
