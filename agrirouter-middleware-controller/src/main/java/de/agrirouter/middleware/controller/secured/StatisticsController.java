package de.agrirouter.middleware.controller.secured;

import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import de.agrirouter.middleware.controller.dto.response.domain.MqttStatisticsResponse;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.mqtt.MqttStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for statistics.
 */
@RestController
@Tag(
        name = "statistics",
        description = "Statistics for the whole application."
)
@RequestMapping(SecuredApiController.API_PREFIX + "/statistics")
public class StatisticsController {

    private final MqttStatistics mqttStatistics;
    private final MqttClientManagementService mqttClientManagementService;
    private final ModelMapper modelMapper;

    public StatisticsController(MqttStatistics mqttStatistics,
                                MqttClientManagementService mqttClientManagementService,
                                ModelMapper modelMapper) {
        this.mqttStatistics = mqttStatistics;
        this.mqttClientManagementService = mqttClientManagementService;
        this.modelMapper = modelMapper;
    }

    /**
     * Get the statistics for the MQTT connections.
     *
     * @return Statistics for the MQTT connections.
     */
    @GetMapping(value = "/mqtt", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "statistics.mqtt",
            description = "Get the statistics for the MQTT connections.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The application has been registered and was created successfully.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = MqttStatisticsResponse.class
                                    ),
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
    public ResponseEntity<MqttStatisticsResponse> getMqttStatistics() {
        var mqttStatisticsResponse = modelMapper.map(mqttStatistics, MqttStatisticsResponse.class);
        mqttStatisticsResponse.setNumberOfConnectedClients(mqttClientManagementService.getNumberOfActiveConnections());
        mqttStatisticsResponse.setNumberOfDisconnectedClients(mqttClientManagementService.getNumberOfInactiveConnections());
        return ResponseEntity.ok(mqttStatisticsResponse);
    }

}
