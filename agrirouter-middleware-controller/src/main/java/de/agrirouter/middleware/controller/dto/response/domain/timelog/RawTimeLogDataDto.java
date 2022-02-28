package de.agrirouter.middleware.controller.dto.response.domain.timelog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.bson.Document;

/**
 * The time log with the raw data.
 */
@Data
@ToString
@Schema(description = "The raw data encapsulated within a transfer object.")
public class RawTimeLogDataDto {

    /**
     * The original time log or device description.
     */
    @Schema(description = "The original time log or device description.")
    private Document document;

}
