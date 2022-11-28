package de.agrirouter.middleware.api.errorhandling.error;

import lombok.Getter;
import lombok.Setter;

/**
 * Error message for parameter validation problems.
 */
@Getter
@Setter
public class ParameterValidationError {

    private String field;
    private String code;

}
