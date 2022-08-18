package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * The status of an endpoint.
 */
@Getter
@Setter
@Schema(description = "The connection status of an endpoint.")
public class EndpointStatusDto {

    /**
     * The last update.
     */
    @Schema(description = "The last update.")
    private LocalDateTime lastUpdate;

    /**
     * The number of messages within the inbox.
     */
    @Schema(description = "The number of messages within the inbox.")
    private int nrOfMessagesWithinTheInbox;

    /**
     * The state of the connection.
     */
    @Schema(description = "The state of the connection.")
    private ConnectionStateDto connectionState;

}
