package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.DeviceDto;
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
@Schema(description = "The response when searching for a machine.")
public class SearchMachineResponse extends Response {

    /**
     * The devices found.
     */
    @Schema(description = "The devices found.")
    List<DeviceDto> devices;

}
