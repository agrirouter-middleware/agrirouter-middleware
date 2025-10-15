package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.NotificationDto;
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
@Schema(description = "The response when asking for notifications of an endpoint.")
public class NotificationsResponse extends Response {

    /**
     * The notifications.
     */
    @Schema(description = "The notifications.")
    List<NotificationDto> notificationDtos;

}
