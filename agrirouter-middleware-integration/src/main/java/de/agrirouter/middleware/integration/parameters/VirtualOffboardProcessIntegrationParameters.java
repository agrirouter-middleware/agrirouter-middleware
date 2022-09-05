package de.agrirouter.middleware.integration.parameters;

import de.agrirouter.middleware.domain.Endpoint;

import java.util.List;

/**
 * Parameter class.
 *
 * @param parentEndpoint     The parent endpoint.
 * @param virtualEndpointIds The endpoint IDs of the virtual endpoints.
 */
public record VirtualOffboardProcessIntegrationParameters(Endpoint parentEndpoint, List<String> virtualEndpointIds) {
}
