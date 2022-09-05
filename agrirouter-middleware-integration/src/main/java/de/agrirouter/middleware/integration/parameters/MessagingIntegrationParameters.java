package de.agrirouter.middleware.integration.parameters;

import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.google.protobuf.ByteString;

import java.util.List;

/**
 * Parameter class.
 *
 * @param externalEndpointId   The external ID of the endpoint.
 * @param technicalMessageType The technical message type.
 * @param recipients           The recipients.
 * @param filename             The filename.
 * @param message              The message.
 * @param teamSetContextId
 */
public record MessagingIntegrationParameters(String externalEndpointId, TechnicalMessageType technicalMessageType,
                                             List<String> recipients, String filename, ByteString message,
                                             String teamSetContextId) {
}
