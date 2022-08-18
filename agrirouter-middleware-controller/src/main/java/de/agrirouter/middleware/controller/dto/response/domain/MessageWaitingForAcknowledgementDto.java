package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

/**
 * A message waiting for ACK.
 */
@Getter
@Setter
@Schema(description = "The message waiting for acknowledgement from the agrirouter.")
public class MessageWaitingForAcknowledgementDto {

    /**
     * The point in time the message has been sent and is waiting.
     */
    @Schema(description = "The point in time the message has been sent and is waiting.")
    private long created;

    /**
     * The point in time the message has been sent and is waiting.
     */
    @Schema(description = "The point in time the message has been sent and is waiting.")
    private Date humanReadableCreated;

    /**
     * The endpoint ID.
     */
    @Schema(description = "The endpoint ID.")
    private String agrirouterEndpointId;

    /**
     * The message ID.
     */
    @Schema(description = "The message ID.")
    private String messageId;

    /**
     * The type of message waiting for response.
     */
    @Schema(description = "The type of message waiting for response.")
    private String technicalMessageType;

    /**
     * Dynamic properties for the message waiting for ACK.
     */
    @Schema(description = "Dynamic properties for the message waiting for ACK.")
    private Map<String, Object> dynamicProperties;

}
