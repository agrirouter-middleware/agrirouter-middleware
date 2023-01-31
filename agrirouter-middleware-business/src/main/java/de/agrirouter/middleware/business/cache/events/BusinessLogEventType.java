package de.agrirouter.middleware.business.cache.events;

/**
 * Event types for the business log.
 */
public enum BusinessLogEventType {

    /**
     * A new task data has been received.
     */
    TASK_DATA_RECEIVED,

    /**
     * A new device description has been received.
     */
    DEVICE_DESCRIPTION_RECEIVED,

    /**
     * A new time log has been received.
     */
    TIME_LOG_RECEIVED,

    /**
     * A new non telemetry message has been received.
     */
    NON_TELEMETRY_MESSAGE_RECEIVED

}
