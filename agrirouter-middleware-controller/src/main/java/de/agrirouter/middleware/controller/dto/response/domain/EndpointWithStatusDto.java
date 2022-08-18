package de.agrirouter.middleware.controller.dto.response.domain;

import de.agrirouter.middleware.domain.enums.EndpointType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * DTO.
 */
@Data
@ToString
@Schema(description = "The status of an endpoint.")
public class EndpointWithStatusDto {

    /**
     * The ID of an endpoint.
     */
    @Schema(description = "The ID of an endpoint.")
    private String agrirouterEndpointId;

    /**
     * The external ID of an endpoint.
     */
    @Schema(description = "The external ID of an endpoint.")
    private String externalEndpointId;

    /**
     * The current status of an endpoint, could be active or deactivated.
     */
    @Schema(description = "The current status of an endpoint, could be active or deactivated.")
    private boolean deactivated;

    /**
     * The internal ID of the application.
     */
    @Schema(description = "The internal ID of the application.")
    private String internalApplicationId;

    /**
     * The ID of the application.
     */
    @Schema(description = "The ID of the application.")
    private String applicationId;

    /**
     * The ID of the application version.
     */
    @Schema(description = "The ID of the application version.")
    private String versionId;

    /**
     * The account ID for this endpoint.
     */
    @Schema(description = "The account ID for this endpoint.")
    private String agrirouterAccountId;

    /**
     * The type of the endpoint.
     */
    @Schema(description = "The type of the endpoint.")
    private EndpointType endpointType;

    /**
     * The current status of the endpoint.
     */
    @Schema(description = "The current status of the endpoint.")
    private EndpointStatusDto endpointStatus;

    /**
     * The errors for the endpoint.
     */
    @Schema(description = "The errors for the endpoint.")
    private List<LogEntryDto> errors;

    /**
     * The warnings for the endpoint.
     */
    @Schema(description = "The warnings for the endpoint.")
    private List<LogEntryDto> warnings;

    /**
     * The messages currently waiting for ACK.
     */
    @Schema(description = "The messages currently waiting for ACK.")
    private List<MessageWaitingForAcknowledgementDto> messagesWaitingForAck;

    /**
     * Number of messages that are currently cached.
     */
    @Schema(description = "Nr. of messages that are cached.")
    private long nrOfMessagesCached;

    /**
     * The message recipients for this endpoint.
     */
    @Schema(description = "The message recipients for this endpoint.")
    private List<MessageRecipientDto> messageRecipients;

}
