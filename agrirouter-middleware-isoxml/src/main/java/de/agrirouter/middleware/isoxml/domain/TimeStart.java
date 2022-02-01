package de.agrirouter.middleware.isoxml.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(int milliseconds) {
        this.milliseconds = milliseconds;
    }

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