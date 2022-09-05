package de.agrirouter.middleware.integration.parameters;

import de.agrirouter.middleware.domain.Endpoint;

/**
 * Parameter class.
 *
 * @param parentEndpoint            The parent endpoint.
 * @param endpointName              The name of the endpoint.
 * @param externalVirtualEndpointId The ID of the virtual endpoint.
 */
public record VirtualOnboardProcessIntegrationParameters(Endpoint parentEndpoint, String endpointName, String externalVirtualEndpointId) {
}
