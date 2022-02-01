package de.agrirouter.middleware.controller.aop;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorKey;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global handler for all internal business exceptions.
 */
@ControllerAdvice
public class CustomExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomExceptionHandler.class);
    private static final ErrorResponse UNKNOWN_ERROR = new ErrorResponse(ErrorKey.UNKNOWN_ERROR.getKey(), "This was an unknown error, please file an issue and see the logs for more details.");

    /**
     * Handling all the internal exceptions.
     *
     * @return -
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handle(BusinessException businessException) {
        LOGGER.error("A business exception occurred.", businessException);
        return ResponseEntity.badRequest().body(new ErrorResponse(businessException.getErrorMessage().getKey().getKey(), businessException.getErrorMessage().getMessage()));
    }

    @ExceptionHandler({RuntimeException.class, Exception.class})
    public ResponseEntity<ErrorResponse> handle(RuntimeException runtimeException) {
        LOGGER.error("A unknown exception occurred.", runtimeException);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UNKNOWN_ERROR);
    }

}
