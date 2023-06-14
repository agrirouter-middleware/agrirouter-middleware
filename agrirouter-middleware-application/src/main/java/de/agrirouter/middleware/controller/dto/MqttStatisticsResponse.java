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

    @Getter
    @Setter
    public static class ConnectionStatistics {
        private long numberOfConnectionLosses;
        private long numberOfCacheMisses;
        private long numberOfClientInitializations;
        private long numberOfDisconnects;
        private long numberOfConnects;
        private long numberOfReconnects;
    }

    @Getter
    @Setter
    public static class MqttMessageStatistics {
        public long payloadReceived;
        private long numberOfMessagesPublished;
        private long numberOfMessagesArrived;
        private long numberOfAcknowledgements;
        private long numberOfPushNotifications;
        private int numberOfCloudRegistrations;
        private long numberOfEndpointListings;
        private int numberOfUnknownMessages;
    }

}
