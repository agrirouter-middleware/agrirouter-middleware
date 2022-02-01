package de.agrirouter.middleware.api.errorhandling;

import de.agrirouter.middleware.api.errorhandling.error.ErrorMessage;

/**
 * Will be thrown in case of any business problem.
 */
public class BusinessException extends RuntimeException {

    /**
     * The error message.
     */
    private final ErrorMessage errorMessage;

    /**
     * Constructor.
     *
     * @param errorMessage -
     */
    public BusinessException(ErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Constructor.
     *
     * @param errorMessage -
     * @param e            -
     */
    public BusinessException(ErrorMessage errorMessage, Exception e) {
        super(e);
        this.errorMessage = errorMessage;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getMessage() {
        return errorMessage.asLogMessage();
    }
}
