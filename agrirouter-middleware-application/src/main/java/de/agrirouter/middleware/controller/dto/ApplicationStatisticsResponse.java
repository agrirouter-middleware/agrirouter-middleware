package de.agrirouter.middleware.controller.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Container for application statistics.
 */
@Getter
@Builder
public class ApplicationStatisticsResponse {

    private final long nrOfTenants;
    private final long nrOfApplications;
    private final long nrOfEndpoints;
    private final long nrOfVirtualEndpoints;
    private final long nrOfConnectedClients;
    private final long nrOfDisconnectedClients;

}
