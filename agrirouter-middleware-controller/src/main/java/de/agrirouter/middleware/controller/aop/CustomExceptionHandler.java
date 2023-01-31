package de.agrirouter.middleware.controller.aop;

import com.dke.data.agrirouter.api.exception.IllegalParameterDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.ParameterValidationException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorKey;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import de.agrirouter.middleware.controller.dto.response.ParameterValidationProblemResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global handler for all internal business exceptions.
 */
@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    /**
     * Handling all the internal exceptions.
     *
     * @return -
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handle(BusinessException businessException) {
        log.error("A business exception occurred.", businessException);
        return ResponseEntity.badRequest().body(new ErrorResponse(businessException.getErrorMessage().getKey().getKey(), businessException.getErrorMessage().getMessage()));
    }

    @ExceptionHandler(ParameterValidationException.class)
    public ResponseEntity<ParameterValidationProblemResponse> handle(ParameterValidationException parameterValidationException) {
        log.error("A parameter validation exception occurred.", parameterValidationException);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ParameterValidationProblemResponse(parameterValidationException.getErrors()));
    }

    @ExceptionHandler({RuntimeException.class, Exception.class})
    public ResponseEntity<ErrorResponse> handle(RuntimeException runtimeException) {
        log.error("A unknown exception occurred.", runtimeException);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(ErrorKey.UNKNOWN_ERROR.getKey(), "This was an unknown error, please file an issue and see the logs for more details."));
    }

    @ExceptionHandler({IllegalParameterDefinitionException.class, InvalidFormatException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handle(Exception exception) {
        log.error("A illegal parameter definition exception occurred.", exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ErrorKey.ILLEGAL_PARAMETER_DEFINITION.getKey(), "The parameter you provided was not valid."));
    }

}
