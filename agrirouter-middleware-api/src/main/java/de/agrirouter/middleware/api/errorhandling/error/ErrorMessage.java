package de.agrirouter.middleware.api.errorhandling.error;

import org.springframework.http.HttpStatus;

/**
 * Class containing global error codes.
 *
 * @param key        Key for the error.
 * @param message    Message.
 * @param httpStatus HTTP status for the error.
 */
public record ErrorMessage(ErrorKey key, String message, HttpStatus httpStatus) {

    /**
     * Return the error message as log message.
     *
     * @return -
     */
    public String asLogMessage() {
        return String.format("[%s] %s", key, message);
    }

}
