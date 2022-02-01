package de.agrirouter.middleware.business.parameters;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Parameters to search for machines.
 */
@Setter
@Getter
@ToString
public class SearchTimeLogPeriodsParameters {

    /**
     * The external endpoint ID.
     */
    private String externalEndpointId;

    /**
     * The list of internal device IDs to search for.
     */
    private List<String> internalDeviceIds;

    /**
     * Filter empty entries, like machines and time periods.
     */
    private boolean filterEmptyEntries;

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
