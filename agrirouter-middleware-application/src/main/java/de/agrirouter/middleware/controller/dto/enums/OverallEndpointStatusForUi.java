package de.agrirouter.middleware.controller.dto.enums;

/**
 * The overall endpoint status.
 */
public record OverallEndpointStatusForUi(Status status, String toolTip) {

    public static OverallEndpointStatusForUi fromMetric(int metric, String toolTip) {
        if (metric < 50) {
            return new OverallEndpointStatusForUi(Status.ERROR, toolTip);
        } else if (metric < 75) {
            return new OverallEndpointStatusForUi(Status.WARNING, toolTip);
        } else {
            return new OverallEndpointStatusForUi(Status.OK, toolTip);
        }
    }

    private enum Status {
        OK, WARNING, ERROR
    }
}
