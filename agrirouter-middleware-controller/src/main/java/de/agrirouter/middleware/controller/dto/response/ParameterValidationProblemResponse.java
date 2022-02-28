package de.agrirouter.middleware.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.springframework.validation.ObjectError;

import java.util.List;

/**
 * Generic error response to map the internal errors and provide clean API description.
 */
@Value
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Generic error response to map parameter validation errors and provide clean API description without publishing internals.")
public class ParameterValidationProblemResponse extends Response {

    @Schema(description = "The validation errors.")
    List<ObjectError> errors;

}
