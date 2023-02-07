package de.agrirouter.middleware.controller.unsecured;

import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import de.agrirouter.middleware.controller.dto.response.VersionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for information purpose.
 */
@RestController
@RequestMapping(UnsecuredApiController.API_PREFIX + "/info")
@Tag(name = "info", description = "Operation for information purpose.")
public class InfoController implements UnsecuredApiController {

    /**
     * The current version of the application.
     */
    @Value("${app.version:unknown}")
    private String currentVersion;

    /**
     * The current build of the application.
     */
    @Value("${app.build:unknown}")
    private String currentBuild;

    /**
     * Returns the version of the application.
     *
     * @return The version of the application
     */
    @Operation(
            operationId = "info.version",
            description = "Get the current version incl. the timestamp of the build.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The current version of the application.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = VersionsResponse.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "In case of an unknown error.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            }
    )
    @GetMapping(value = "/version", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VersionsResponse> version() {
        return ResponseEntity.ok(new VersionsResponse(String.format("%s (Build: %s)", currentVersion, currentBuild)));
    }

}
