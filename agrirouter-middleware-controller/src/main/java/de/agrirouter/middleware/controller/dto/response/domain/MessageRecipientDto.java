package de.agrirouter.middleware.controller.dto.response.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * One of the message recipients for the endpoint.
 */
@Getter
@Setter
@Schema(description = "The status of an endpoint.")
public class MessageRecipientDto {

    /**
     * The agrirouter© endpoint ID.
     */
    private String agrirouterEndpointId;

    /**
     * The name of the endpoint, defined by the user.
     */
    private String endpointName;

    /**
     * The type of the endpoint.
     */
    private String endpointType;

    /**
     * The external ID.
     */
    private String externalId;

    /**
     * The technical message type.
     */
    private String technicalMessageType;

    /**
     * The direction.
     */
    private String direction;

    /**
     * Indicator if the entry was cached.
     */
    private boolean cached;

    /**
     * last update timestamp
     */
    private String timestamp;

}
