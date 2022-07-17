package de.agrirouter.middleware.business.cache.registration;

import de.agrirouter.middleware.business.parameters.RegisterMachineParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Transient cache for messages.
 */
@Slf4j
@Component
public class TransientMachineRegistrationCache {

    /**
     * The time to live in seconds.
     */
    @Value("${app.cache.transient-machine-registration-cache.time-to-live-in-seconds}")
    private int timeToLiveInSeconds;

    /**
     * The message cache.
     */
    private final Map<String, MachineRegistrationCacheEntry> machineRegistrationCache = new HashMap<>();

    /**
     * Create a cache entry.
     *
     * @param externalEndpointId        The external endpoint ID.
     * @param teamSetContextId          The team set context ID.
     * @param registerMachineParameters Parameters for registering a machine.
     */
    public void put(String externalEndpointId, String teamSetContextId, RegisterMachineParameters registerMachineParameters) {
        log.info("Saving device description to cache.");
        log.trace("External endpoint ID: {}", externalEndpointId);
        machineRegistrationCache.put(externalEndpointId, new MachineRegistrationCacheEntry(externalEndpointId,
                teamSetContextId,
                registerMachineParameters,
                Instant.now().getEpochSecond(),
                timeToLiveInSeconds));
    }

    /**
     * Get a cache entry.
     *
     * @param externalEndpointId The external endpoint ID.
     * @return The cache entry.
     */
    public Optional<MachineRegistrationCacheEntry> pop(String externalEndpointId) {
        log.info("Removing device description from cache.");
        log.trace("External endpoint ID: {}", externalEndpointId);
        final var machineRegistrationCacheEntry = machineRegistrationCache.get(externalEndpointId);
        if (machineRegistrationCacheEntry != null) {
            machineRegistrationCache.remove(externalEndpointId);
            log.info("Found a device description in cache.");
            if (machineRegistrationCacheEntry.isExpired()) {
                log.debug("Device description expired. Skipping.");
                return Optional.empty();
            } else {
                return Optional.of(machineRegistrationCacheEntry);
            }
        } else {
            log.info("No device description found in cache.");
            return Optional.empty();
        }
    }

    /**
     * Cache entry.
     *
     * @param externalEndpointId        The external endpoint ID.
     * @param teamSetContextId          Team set context ID.
     * @param registerMachineParameters Parameters for registering a new machine.
     * @param createdAt                 The time when the cache entry was created.
     * @param ttl                       The time to live in seconds.
     */
    public record MachineRegistrationCacheEntry(
            String externalEndpointId,
            String teamSetContextId,
            RegisterMachineParameters registerMachineParameters,
            long createdAt,
            long ttl
    ) {

        /**
         * Check if the cache entry is expired.
         *
         * @return -
         */
        public boolean isExpired() {
            long now = Instant.now().getEpochSecond();
            return now - createdAt > ttl;
        }

    }
}

