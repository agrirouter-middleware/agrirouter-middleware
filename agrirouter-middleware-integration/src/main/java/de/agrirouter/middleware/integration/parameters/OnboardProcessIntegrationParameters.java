package de.agrirouter.middleware.integration.parameters;

/**
 * * Parameter class.
 *
 * @param applicationId    The ID of the application.
 * @param versionId        The ID of the version.
 * @param endpointId       The ID of the endpoint.
 * @param registrationCode The registration code.
 */
public record OnboardProcessIntegrationParameters(String applicationId, String versionId, String endpointId,
                                                  String registrationCode) {
}
