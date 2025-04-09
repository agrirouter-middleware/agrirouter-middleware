package de.agrirouter.middleware.isoxml.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Setter
@Getter
public class TimeStart {

    /**
     * Constant start value for the time.
     */
    private static final LocalDateTime gpsTimeStart = LocalDateTime.of(1980, 1, 1, 0, 0, 0, 0);

    /**
     * The days.
     */
    private int days;


    /**
     * The days.
     */
    private int milliseconds;

    @Override
    public String toString() {
        return "TimeStart{" +
                "days=" + days +
                ", milliseconds=" + milliseconds +
                '}';
    }

    /**
     * To instant.
     *
     * @param zoneId the zone id
     * @return the instant
     */
    public Instant toInstant(ZoneId zoneId) {
        return gpsTimeStart.plusDays(days).plusNanos(milliseconds * 1000000L).atZone(zoneId).toInstant();
    }

    /**
     * To local instant.
     *
     * @return the instant
     */
    public Instant toLocalInstant() {
        return toInstant(ZoneId.systemDefault());
    }

}