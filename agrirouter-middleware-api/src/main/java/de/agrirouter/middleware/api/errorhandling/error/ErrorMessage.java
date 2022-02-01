package de.agrirouter.middleware.api.errorhandling.error;

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

    public ErrorMessage(ErrorKey key, String message) {
        this.key = key;
        this.message = message;
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
