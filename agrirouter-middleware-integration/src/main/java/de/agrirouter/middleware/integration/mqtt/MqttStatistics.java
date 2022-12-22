package de.agrirouter.middleware.integration.mqtt;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

/**
 * Transient statistics for the MQTT connections.
 */
@Getter
@Component
@ApplicationScope
public class MqttStatistics {

    private long numberOfConnectionLosses;
    private long numberOfCacheMisses;
    private long numberOfMessagesArrived;
    private long numberOfAcknowledgements;
    private long numberOfPushNotifications;
    private int numberOfCloudRegistrations;
    private long numberOfEndpointListings;
    private int numberOfUnknownMessages;
    private long numberOfClientInitializations;
    private long numberOfDisconnects;
    private long numberOfStaleConnectionsRemovals;

    /**
     * Increase the number of connection losses.
     */
    public void increaseNumberOfConnectionLosses() {
        numberOfConnectionLosses++;
    }

    /**
     * Increase the number of cache misses.
     */
    public void increaseNumberOfCacheMisses() {
        numberOfCacheMisses++;
    }

    /**
     * Increase the number of messages arrived.
     */
    public void increaseNumberOfMessagesArrived() {
        numberOfMessagesArrived++;
    }

    /**
     * Increase the number of acknowledgements.
     */
    public void increaseNumberOfAcknowledgements() {
        numberOfAcknowledgements++;
    }

    public void increaseNumberOfPushNotifications() {
        numberOfPushNotifications++;
    }

    public void increaseNumberOfCloudRegistrations() {
        numberOfCloudRegistrations++;
    }

    public void increaseNumberOfEndpointListings() {
        numberOfEndpointListings++;
    }

    public void increaseNumberOfUnknownMessages() {
        numberOfUnknownMessages++;
    }

    public void increaseNumberOfClientInitializations() {
        numberOfClientInitializations++;
    }

    public void increaseNumberOfDisconnects() {
        numberOfDisconnects++;
    }

    public void increaseNumberOfStaleConnectionsRemovals() {
        numberOfStaleConnectionsRemovals++;
    }
}
