package de.agrirouter.middleware.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for time operations.
 */
@Service
public class TimerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimerService.class);

    /**
     * Common intervalls.
     */
    public static final class TimeInterval {
        public static final long A_SECOND = 1000L;
        public static final long FIVE_SECONDS = A_SECOND * 5;
        public static final long A_MINUTE = A_SECOND * 60;
        public static final long AN_HOUR = A_MINUTE * 60;
    }

    /**
     * Waiting for some time.
     */
    public void waitSomeTime() {
        waitSomeTime(TimeInterval.A_SECOND * 3);
    }

    /**
     * Waiting for some time.
     *
     * @param millis Time to wait in milli seconds.
     */
    public void waitSomeTime(long millis) {
        try {
            LOGGER.debug("Start waiting ...");
            Thread.sleep(millis);
            LOGGER.debug("Continue ...");
        } catch (InterruptedException e) {
            LOGGER.warn("Waiting was interrupted. Therefore returning to the task immediately.");
        }
    }


}
