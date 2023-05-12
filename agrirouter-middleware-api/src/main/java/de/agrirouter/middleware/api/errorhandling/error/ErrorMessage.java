package de.agrirouter.middleware.api.errorhandling.error;

import org.springframework.http.HttpStatus;

/**
 * Class containing global error codes.
 */
public class ErrorMessage {

    /**
     * Key for the error.
     */
    private final ErrorKey key;

    /**
     * Message.
     */
    private final String message;

    /**
     * HTTP status for the error.
     */
    private final HttpStatus httpStatus;

    public ErrorMessage(ErrorKey key, String message) {
        this.key = key;
        this.message = message;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public ErrorMessage(ErrorKey key, String message, HttpStatus httpStatus) {
        this.key = key;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public ErrorKey getKey() {
        return key;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Return the error message as log message.
     *
     * @return -
     */
    public String asLogMessage() {
        return String.format("[%s] %s", key, message);
    }

}
