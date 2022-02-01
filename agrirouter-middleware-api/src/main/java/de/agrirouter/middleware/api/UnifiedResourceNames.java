package de.agrirouter.middleware.api;

/**
 * Container for the URN prefixes of the application.
 */
public interface UnifiedResourceNames {

    /**
     * Internal prefix.
     */
    String APP_PREFIX = "urn:agrirouter-middleware:";

    /**
     * Prefix for all applications.
     */
    String APPLICATION_PREFIX = APP_PREFIX + "application:";

    /**
     * Prefix for all team sets.
     */
    String TEAMSET_PREFIX = APP_PREFIX + "teamset:";

    /**
     * Prefix for all devices.
     */
    String DEVICE_PREFIX = APP_PREFIX + "device:";

}
