package de.agrirouter.middleware.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * DTO.
 */
@Getter
@Setter
public class MqttStatisticsResponse {

    private ConnectionStatistics connectionStatistics;

    private MqttMessageStatistics mqttMessageStatistics;

    private ContentMessageStatistics contentMessageStatistics;

    @Getter
    @Setter
    public static class ConnectionStatistics {
        private long numberOfConnectionLosses;

        private long numberOfCacheMisses;

        private long numberOfClientInitializations;

        private long numberOfDisconnects;
    }

    @Getter
    @Setter
    public static class MqttMessageStatistics {
        private long numberOfMessagesArrived;

        private long numberOfAcknowledgements;

        private long numberOfPushNotifications;

        private int numberOfCloudRegistrations;

        private long numberOfEndpointListings;

        private int numberOfUnknownMessages;
    }

    @Getter
    @Setter
    public static class ContentMessageStatistics {
        private Map<String, Integer> numberOfContentMessagesReceived;
    }
}
