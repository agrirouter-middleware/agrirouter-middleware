package de.agrirouter.middleware.controller.dto.response;

import com.dke.data.agrirouter.impl.common.UtcTimeService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Common interface.
 */
@Getter
@Schema(description = "Common response schema, holding shared values.")
class Response {

    /**
     * The current timestamp
     */
    @Schema(description = "The timestamp (UTC) of the response.")
    LocalDateTime timestamp = LocalDateTime.from(UtcTimeService.now());

}
