package de.agrirouter.middleware.api.logging;

/**
 * Parameter object for endpoint business actions.
 */
public record EndpointLogInformation(String externalEndpointId, String agrirouterEndpointId) {
}
