package de.agrirouter.middleware.business.parameters;

import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import lombok.*;

import java.util.List;

/**
 * Parameters to publish non telemetry data messages.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PublishNonTelemetryDataParameters {

    /**
     * The ID of the endpoint sending the message.
     */
    private String externalEndpointId;

    /**
     * The message itself.
     */
    private String base64EncodedMessageContent;

    /**
     * The content message type.
     */
    private TechnicalMessageType contentMessageType;

    /**
     * The name of the file.
     */
    private String filename;

    /**
     * The recipients for direct sending. If there is a recipient, the file will not be published.
     */
    private List<String> recipients;

}
