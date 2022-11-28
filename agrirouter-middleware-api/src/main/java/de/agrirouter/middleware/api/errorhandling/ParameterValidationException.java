package de.agrirouter.middleware.api.errorhandling;

import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.errorhandling.error.ParameterValidationError;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

/**
 * Will be thrown if there are any kind of validation errors.
 */
@Getter
public class ParameterValidationException extends BusinessException {

    private final List<ParameterValidationError> errors;

    public ParameterValidationException(Errors errors) {
        super(ErrorMessageFactory.parameterValidationProblem());

        this.errors = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        errors.getAllErrors().forEach(objectError -> {
            var parameterValidationError = modelMapper.map(objectError, ParameterValidationError.class);
            this.errors.add(parameterValidationError);
        });
    }

}
