package de.agrirouter.middleware.controller.secured;

import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.controller.SecuredApiController;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to onboard a single endpoint.
 */
@RestController
@RequestMapping(SecuredApiController.API_PREFIX + "/farming-software")
@Tag(
        name = "farming software management",
        description = "Operations for farming software management, i.e. onboard process for endpoints or revoking endpoints."
)
public class FarmingSoftwareController implements SecuredApiController {

    private final EndpointService endpointService;

    public FarmingSoftwareController(EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    /**
     * Remove the endpoint from the AR and delete the data.
     *
     * @return HTTP 200.
     */
    @DeleteMapping("/{externalEndpointId}")
    @Operation(
            operationId = "farming-software.revoke",
            description = "Revoke a farming software from the middleware and remove all of its data, incl. virtual endpoints, messages, logs, etc.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The revoke process was successful.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a business exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "In case of an unknown error.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            }
    )
    public ResponseEntity<Void> revokeFarmingSoftware(@Parameter(description = "The external endpoint id.", required = true) @PathVariable String externalEndpointId) {
        endpointService.revoke(externalEndpointId);
        return ResponseEntity.ok().build();
    }

}
