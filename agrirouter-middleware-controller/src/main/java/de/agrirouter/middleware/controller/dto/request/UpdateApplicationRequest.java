package de.agrirouter.middleware.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * The request body for the update application request.
 */
@Getter
@Setter
@ToString
@Schema(description = "The request body to update an existing application within the middleware.")
public class UpdateApplicationRequest {

    /**
     * The internal ID of the application.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The internal ID of the application.")
    private String internalApplicationId;

    /**
     * The name of the application.
     */
    @Schema(description = "The name of the application.")
    private String name;

    /**
     * The private key of the application.
     */
    @Schema(description = "The private key of the application. Only needed if the application is an instance of a farming software or telemetry platform. The private key has to be Base64 encoded.")
    private String base64EncodedPrivateKey;

    /**
     * The public key of the application.
     */
    @Schema(description = "The public key of the application. Only needed if the application is an instance of a farming software or telemetry platform. The public key has to be Base64 encoded.")
    private String base64EncodedPublicKey;

    /**
     * The redirect URL for the application.
     */
    @Schema(description = "The redirect URL for the application. To configure a custom redirect URL that matches the location of the service.")
    private String redirectUrl;

}
