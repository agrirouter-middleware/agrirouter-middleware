package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO.
 */
@Getter
@Setter
@Schema(description = "Statistics for the MQTT connections.")
public class MqttStatisticsResponse {

    @Schema(description = "The number of connection losses.")
    private long numberOfConnectionLosses;

    @Schema(description = "The number of cache misses.")
    private long numberOfCacheMisses;

    @Schema(description = "The number of messages arrived.")
    private long numberOfMessagesArrived;

    @Schema(description = "The number of acknowledgements.")
    private long numberOfAcknowledgements;

    @Schema(description = "The number of push notifications.")
    private long numberOfPushNotifications;

    @Schema(description = "The number of cloud registrations.")
    private int numberOfCloudRegistrations;

    @Schema(description = "The number of endpoint listings.")
    private long numberOfEndpointListings;

    @Schema(description = "The number of unknown messages.")
    private int numberOfUnknownMessages;

    @Schema(description = "The number of client initializations.")
    private long numberOfClientInitializations;

    @Schema(description = "The number of disconnects.")
    private long numberOfDisconnects;

    @Schema(description = "The number of stale connections removals.")
    private long numberOfStaleConnectionsRemovals;

    @Schema(description = "The number of connected clients.")
    private long numberOfConnectedClients;

    @Schema(description = "The number of disconnected clients.")
    private long numberOfDisconnectedClients;
}
