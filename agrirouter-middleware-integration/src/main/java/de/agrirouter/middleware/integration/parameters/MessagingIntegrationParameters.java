package de.agrirouter.middleware.integration.parameters;

import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.google.protobuf.ByteString;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Messaging parameters for sending and publishing messages.
 */
@Getter
@Setter
@ToString
public class MessagingIntegrationParameters {

    /**
     * The ID of the endpoint sending the message.
     */
    private String externalEndpointId;

    /**
     * The technical message type of the message.
     */
    private TechnicalMessageType technicalMessageType;

    /**
     * A list of endpoint IDs as recipients.
     */
    private List<String> recipients;

    /**
     * Name of the file, optional.
     */
    private String filename;

    /**
     * The message itself.
     */
    private ByteString message;

    /**
     * The team set context ID in case of live telemetry data sending.
     */
    private String teamSetContextId;

}
