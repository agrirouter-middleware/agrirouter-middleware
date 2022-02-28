package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.MessageRecipientDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * Response class for better API design.
 */
@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "The response when asking for the recipients of an endpoint.")
public class EndpointRecipientsResponse extends Response {

    /**
     * The message recipients for this endpoint.
     */
    @Schema(description = "The message recipients for this endpoint.")
    List<MessageRecipientDto> messageRecipients;

}
