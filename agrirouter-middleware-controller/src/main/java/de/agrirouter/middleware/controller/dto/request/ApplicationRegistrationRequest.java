package de.agrirouter.middleware.controller.dto.request;

import de.agrirouter.middleware.domain.enums.ApplicationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * The request body for the application registration request.
 */
@Getter
@Setter
@ToString
@Schema(description = "The request body to register a application within the middleware.")
public class ApplicationRegistrationRequest {

    /**
     * The name of the application.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The name of the application.")
    private String name;

    /**
     * The ID of the application.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The agrirouter© ID of the application.")
    private String applicationId;

    /**
     * The version of the application. Each agrirouter© version creates a new application in the middleware.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The version of the application. Each agrirouter© version creates a new application in the middleware.")
    private String versionId;

    /**
     * The private key of the application.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The private key of the application. Only needed if the application is an instance of a farming software or telemetry platform. The private key has to be Base64 encoded.")
    private String base64EncodedPrivateKey;

    /**
     * The public key of the application.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The public key of the application. Only needed if the application is an instance of a farming software or telemetry platform. The public key has to be Base64 encoded.")
    private String base64EncodedPublicKey;

    /**
     * The type of the application.
     */
    @NotNull
    @Schema(description = "The type of the application.", required = true)
    private ApplicationType applicationType;

    /**
     * The redirect URL for the application.
     */
    @Schema(description = "The redirect URL for the application. To configure a custom redirect URL that matches the location of the service.")
    private String redirectUrl;

}
