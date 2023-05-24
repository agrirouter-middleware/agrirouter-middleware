package de.agrirouter.middleware.controller.dto.response.domain;

import de.agrirouter.middleware.integration.mqtt.MqttStatistics;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * DTO.
 */
@Getter
@Setter
@Schema(description = "Statistics for the MQTT connections.")
public class MqttStatisticsResponse {

    @Schema(description = "Statistics for the MQTT connections.")
    private MqttStatistics.ConnectionStatistics connectionStatistics;

    @Schema(description = "Statistics for the MQTT messages.")
    private MqttStatistics.MqttMessageStatistics mqttMessageStatistics;

    @Schema(description = "Statistics for the MQTT content messages.")
    private MqttStatistics.ContentMessageStatistics contentMessageStatistics;

    @Schema(description = "Number of connected clients.")
    private long numberOfConnectedClients;

    @Schema(description = "Number of disconnected clients.")
    private long numberOfDisconnectedClients;

    @Getter
    @Setter
    public static class ConnectionStatistics {
        @Schema(description = "Number of connection losses.")
        private long numberOfConnectionLosses;

        @Schema(description = "Number of cache misses.")
        private long numberOfCacheMisses;

        @Schema(description = "Number of client initializations.")
        private long numberOfClientInitializations;

        @Schema(description = "Number of disconnects.")
        private long numberOfDisconnects;
    }

    @Getter
    @Setter
    public static class MqttMessageStatistics {
        @Schema(description = "Number of messages arrived.")
        private long numberOfMessagesArrived;

        @Schema(description = "Number of acknowledgements.")
        private long numberOfAcknowledgements;

        @Schema(description = "Number of push notifications.")
        private long numberOfPushNotifications;

        @Schema(description = "Number of cloud registrations.")
        private int numberOfCloudRegistrations;

        @Schema(description = "Number of endpoint listings.")
        private long numberOfEndpointListings;

        @Schema(description = "Number of unknown messages.")
        private int numberOfUnknownMessages;
    }

    @Getter
    @Setter
    public static class ContentMessageStatistics {
        @Schema(description = "Number of messages received.")
        private Map<String, Integer> numberOfContentMessagesReceived;
    }
}
