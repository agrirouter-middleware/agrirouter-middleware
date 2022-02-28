package de.agrirouter.middleware.api.errorhandling;

import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import org.springframework.validation.Errors;

/**
 * Will be thrown if there are any kind of validation errors.
 */
public class ParameterValidationException extends BusinessException {

    private final Errors errors;

    public ParameterValidationException(Errors errors) {
        super(ErrorMessageFactory.parameterValidationProblem());
        this.errors = errors;
    }

    public Errors getErrors() {
        return errors;
    }
}
