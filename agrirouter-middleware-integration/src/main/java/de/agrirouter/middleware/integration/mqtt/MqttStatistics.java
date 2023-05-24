package de.agrirouter.middleware.integration.mqtt;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Transient statistics for the MQTT connections.
 */
@Getter
@Component
public class MqttStatistics {

    private final ConnectionStatistics connectionStatistics;
    private final MqttMessageStatistics mqttMessageStatistics;
    private final ContentMessageStatistics contentMessageStatistics;

    public MqttStatistics() {
        connectionStatistics = new ConnectionStatistics();
        mqttMessageStatistics = new MqttMessageStatistics();
        contentMessageStatistics = new ContentMessageStatistics();
        contentMessageStatistics.numberOfContentMessagesReceived = new HashMap<>();
    }


    public void increaseNumberOfConnectionLosses() {
        connectionStatistics.numberOfConnectionLosses++;
    }

    public void increaseNumberOfCacheMisses() {
        connectionStatistics.numberOfCacheMisses++;
    }

    public void increaseNumberOfClientInitializations() {
        connectionStatistics.numberOfClientInitializations++;
    }

    public void increaseNumberOfDisconnects() {
        connectionStatistics.numberOfDisconnects++;
    }

    public void increaseNumberOfMessagesArrived() {
        mqttMessageStatistics.numberOfMessagesArrived++;
    }

    public void increaseNumberOfAcknowledgements() {
        mqttMessageStatistics.numberOfAcknowledgements++;
    }

    public void increaseNumberOfPushNotifications() {
        mqttMessageStatistics.numberOfPushNotifications++;
    }

    public void increaseNumberOfCloudRegistrations() {
        mqttMessageStatistics.numberOfCloudRegistrations++;
    }

    public void increaseNumberOfEndpointListings() {
        mqttMessageStatistics.numberOfEndpointListings++;
    }

    public void increaseNumberOfUnknownMessages() {
        mqttMessageStatistics.numberOfUnknownMessages++;
    }

    public void increaseNumberOfContentMessagesReceived(String technicalMessageType) {
        var counter = contentMessageStatistics.numberOfContentMessagesReceived.get(technicalMessageType);
        if (counter == null) {
            counter = 0;
        }
        counter++;
        contentMessageStatistics.numberOfContentMessagesReceived.put(technicalMessageType, counter);
    }


    @Getter
    public static class ConnectionStatistics {
        private long numberOfConnectionLosses;
        private long numberOfCacheMisses;
        private long numberOfClientInitializations;
        private long numberOfDisconnects;
    }

    @Getter
    public static class MqttMessageStatistics {
        private long numberOfMessagesArrived;
        private long numberOfAcknowledgements;
        private long numberOfPushNotifications;
        private int numberOfCloudRegistrations;
        private long numberOfEndpointListings;
        private int numberOfUnknownMessages;
    }

    @Getter
    public static class ContentMessageStatistics {
        private Map<String, Integer> numberOfContentMessagesReceived;

    }
}
