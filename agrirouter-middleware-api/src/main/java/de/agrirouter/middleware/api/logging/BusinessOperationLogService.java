package de.agrirouter.middleware.api.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service to log business action in a certain format.
 */
@Slf4j
@Service
public class BusinessOperationLogService {

    public static final String NA = "N/A";

    /**
     * Log a business operation.
     *
     * @param endpointLogInformation -
     * @param message                -
     * @param objects                -
     */
    public void log(EndpointLogInformation endpointLogInformation, String message, Object... objects) {
        final var enhancedMessage = String.format("[eid: '%s'][aid: '%s'] %s",
                endpointLogInformation.externalEndpointId(),
                endpointLogInformation.agrirouterEndpointId(),
                message);
        log.info(enhancedMessage, objects);
    }

}
