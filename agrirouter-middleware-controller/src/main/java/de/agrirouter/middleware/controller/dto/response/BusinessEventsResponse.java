package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.business.cache.events.BusinessEventType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

/**
 * Response object for business events of an endpoint.
 */
@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Response object for business events of an endpoint.")
public class BusinessEventsResponse {

    /**
     * External endpoint ID.
     */
    @Schema(description = "External endpoint ID.")
    private String externalEndpointId;

    /**
     * Business events.
     */
    @Schema(description = "Business events.")
    private Map<BusinessEventType, Instant> businessEvents;

}
