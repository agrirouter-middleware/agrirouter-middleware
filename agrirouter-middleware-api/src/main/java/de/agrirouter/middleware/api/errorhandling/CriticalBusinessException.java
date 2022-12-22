package de.agrirouter.middleware.api.errorhandling;

import de.agrirouter.middleware.api.errorhandling.error.ErrorMessage;
import lombok.Getter;

/**
 * Will be thrown in case of any critical business problem that has to be handled.
 */
public class CriticalBusinessException extends Exception {

    /**
     * The error message.
     */
    @Getter
    private final ErrorMessage errorMessage;

    /**
     * Constructor.
     *
     * @param errorMessage -
     */
    public CriticalBusinessException(ErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String getMessage() {
        return errorMessage.asLogMessage();
    }
}
