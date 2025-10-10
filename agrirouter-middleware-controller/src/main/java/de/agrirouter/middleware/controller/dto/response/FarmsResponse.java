package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.controller.dto.response.domain.FarmDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Response for all farms for the account.")
public class FarmsResponse extends Response {

    @Schema(description = "The external endpoint id.")
    String externalEndpointId;

    @Schema(description = "The farms for the account.")
    List<FarmDto> farms;

}
