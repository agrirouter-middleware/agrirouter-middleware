package de.agrirouter.middleware.business.parameters;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PublishTimeLogParameters {

    /**
     * The ID of the endpoint sending the message.
     */
    private String externalEndpointId;

    /**
     * The message itself.
     */
    private String base64EncodedTimeLog;

    /**
     * The team set context ID in case of live telemetry data sending.
     */
    private String teamSetContextId;

}
