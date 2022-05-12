package de.agrirouter.middleware.business.parameters;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

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
     * The beginning of the time interval.
     */
    private Long sendFrom;

    /**
     * The end of the time interval.
     */
    private Long sendTo;

    /**
     * The DDIs to list, if null or empty all DDIs will be listed and no filter will be applied.
     */
    private Set<Integer> ddisToList;

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
