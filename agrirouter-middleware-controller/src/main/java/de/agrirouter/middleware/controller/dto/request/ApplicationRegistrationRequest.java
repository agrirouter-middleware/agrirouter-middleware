package de.agrirouter.middleware.controller.dto.request;

import agrirouter.request.payload.endpoint.Capabilities;
import de.agrirouter.middleware.controller.dto.request.router_device.RouterDevice;
import de.agrirouter.middleware.domain.enums.ApplicationType;
import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

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
    @NotNull(message = "The name of the application is mandatory.")
    @NotBlank(message = "The name of the application is mandatory.")
    @Schema(description = "The name of the application.")
    private String name;

    /**
     * The ID of the application.
     */
    @NotNull(message = "The agrirouter© ID of the application is mandatory.")
    @NotBlank(message = "The agrirouter© ID of the application is mandatory.")
    @Schema(description = "The agrirouter© ID of the application.")
    private String applicationId;

    /**
     * The version of the application. Each agrirouter© version creates a new application in the middleware.
     */
    @NotNull(message = "The version of the application is mandatory.")
    @NotBlank(message = "The version of the application is mandatory.")
    @Schema(description = "The version of the application. Each agrirouter© version creates a new application in the middleware.")
    private String versionId;

    /**
     * The private key of the application.
     */
    @NotNull(message = "The private key of the application is mandatory.")
    @NotBlank(message = "The private key of the application is mandatory.")
    @Schema(description = "The private key of the application. Only needed if the application is an instance of a farming software or telemetry platform. The private key has to be Base64 encoded.")
    private String base64EncodedPrivateKey;

    /**
     * The public key of the application.
     */
    @NotNull(message = "The public key of the application is mandatory.")
    @NotBlank(message = "The public key of the application is mandatory.")
    @Schema(description = "The public key of the application. Only needed if the application is an instance of a farming software or telemetry platform. The public key has to be Base64 encoded.")
    private String base64EncodedPublicKey;

    /**
     * The type of the application.
     */
    @NotNull(message = "The type of the application is mandatory.")
    @Schema(description = "The type of the application.")
    private ApplicationType applicationType;

    /**
     * The redirect URL for the application.
     */
    @Schema(description = "The redirect URL for the application. To configure a custom redirect URL that matches the location of the service.")
    private String redirectUrl;

    /**
     * The router device.
     */
    @NotNull(message = "The router device is mandatory.")
    @Schema(description = "The router device.")
    private RouterDevice routerDevice;

    /**
     * The list of supported technical message types.
     */
    @NotNull(message = "The list of supported technical message types is mandatory.")
    @NotEmpty(message = "The list of supported technical message types is mandatory.")
    @Schema(description = "The list of supported technical message types.")
    private List<@Valid SupportedTechnicalMessageTypeDto> supportedTechnicalMessageTypes;

    /**
     * The representation of a supported technical message type.
     */
    @Getter
    @Setter
    @ToString
    @Schema(description = "Representation of a technical message type to add.")
    public static class SupportedTechnicalMessageTypeDto {

        /**
         * The technical message type, that the application does support, i.e. TaskData, EFDI, etc.
         */
        @NotNull(message = "The technical message type is mandatory.")
        @Getter
        @Schema(description = "The technical message type, that the application does support, i.e. TaskData, EFDI, etc.")
        private TemporaryContentMessageType technicalMessageType;

        /**
         * The direction the message type can be handled, i.e. SEND, RECEIVE, SEND_RECEIVE.
         */
        @NotNull(message = "The direction of the message type is mandatory.")
        @Schema(description = "The direction the message type can be handled, i.e. SEND, RECEIVE, SEND_RECEIVE.")
        private Capabilities.CapabilitySpecification.Direction direction;

    }
}
