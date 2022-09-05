package de.agrirouter.middleware.integration.parameters;

/**
 * Parameter class.
 *
 * @param applicationId      The ID of the application.
 * @param versionId          The ID of the version.
 * @param externalEndpointId The external ID of the endpoint.
 * @param registrationCode   The registration code.
 * @param privateKey         The private key.
 * @param publicKey          The public key.
 */
public record SecuredOnboardProcessIntegrationParameters(String applicationId, String versionId,
                                                         String externalEndpointId, String registrationCode,
                                                         String privateKey, String publicKey) {
}
