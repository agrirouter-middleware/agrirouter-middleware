package de.agrirouter.middleware.api;

import java.util.UUID;

/**
 * Internal factory for ID generation.
 */
public final class IdFactory {

    private IdFactory() {
        // Hidden
    }

    /**
     * Generate a new team set context ID.
     *
     * @return -
     */
    public static String teamSetContextId() {
        return UnifiedResourceNames.TEAMSET_PREFIX + UUID.randomUUID();
    }

    /**
     * Generate a new team set context ID.
     *
     * @return -
     */
    public static String deviceId() {
        return UnifiedResourceNames.DEVICE_PREFIX + UUID.randomUUID();
    }

    /**
     * Create a new application ID.
     *
     * @return -
     */
    public static String applicationId() {
        return UnifiedResourceNames.APPLICATION_PREFIX + UUID.randomUUID();
    }

    /**
     * Return a new default tenant ID.
     *
     * @return -
     */
    public static String tenantId() {
        return UUID.randomUUID().toString();
    }

}
