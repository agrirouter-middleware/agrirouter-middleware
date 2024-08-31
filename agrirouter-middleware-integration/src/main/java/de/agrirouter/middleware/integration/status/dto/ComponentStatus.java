package de.agrirouter.middleware.integration.status.dto;

/**
 * Current status of the component.
 */
public enum ComponentStatus {

    OPERATIONAL, DEGRADED_PERFORMANCE, PARTIAL_OUTAGE, MAJOR_OUTAGE, UNKNOWN;

    /**
     * Get the status of the component.
     *
     * @param status - Status of the component.
     * @return - Status of the component.
     */
    public static ComponentStatus parse(String status) {
        return switch (status) {
            case "under_maintenance", "major_outage" -> MAJOR_OUTAGE;
            case "operational" -> OPERATIONAL;
            case "degraded_performance" -> DEGRADED_PERFORMANCE;
            case "partial_outage" -> PARTIAL_OUTAGE;
            default -> UNKNOWN;
        };
    }

    /**
     * Check if the agrirouter© is operational.
     *
     * @return - true if operational, false otherwise.
     */
    public boolean isOperational() {
        return this == OPERATIONAL || this == DEGRADED_PERFORMANCE;
    }
}
