package de.agrirouter.middleware.business.parameters;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Parameter class to fetch messages for a time log period.e
 */
@Getter
@Setter
@ToString
public class MessagesForTimeLogPeriodParameters {

    /**
     * The ID of the device.
     */
    private String internalDeviceId;

    /**
     * The team set context ID.
     */
    private String teamSetContextId;

    /**
     * The ID of the time log period.
     */
    private String timeLogPeriodId;

    /**
     * The beginning of the time interval.
     */
    private Long sendFrom;

    /**
     * The end of the time interval.
     */
    private Long sendTo;

    /**
     * Should be filtered by time?
     *
     * @return -
     */
    public boolean shouldSearchInASpecificTimePeriod() {
        return null != timeLogPeriodId;
    }

    /**
     * Should be filtered by time?
     *
     * @return -
     */
    public boolean shouldFilterByTime() {
        return null != sendFrom || null != sendTo;
    }

    /**
     * Return the value or the default.
     *
     * @return -
     */
    public long getSendFromOrDefault() {
        return null != sendFrom ? sendFrom : Long.MIN_VALUE;
    }

    /**
     * Return the value or the default.
     *
     * @return -
     */
    public long getSendToOrDefault() {
        return null != sendTo ? sendTo : Long.MAX_VALUE;
    }

}
