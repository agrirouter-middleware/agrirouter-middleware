package de.agrirouter.middleware.business.parameters;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

/**
 * Parameters to publish non telemetry data messages.
 */
@Getter
@Setter
@ToString
public class SearchNonTelemetryDataParameters {

    /**
     * The ID of the endpoint sending the message.
     */
    private String externalEndpointId;

    /**
     * The content message type.
     */
    private Set<ContentMessageType> technicalMessageTypes;

    /**
     * The beginning of the time interval.
     */
    private Long sendFrom;

    /**
     * The end of the time interval.
     */
    private Long sendTo;

}
