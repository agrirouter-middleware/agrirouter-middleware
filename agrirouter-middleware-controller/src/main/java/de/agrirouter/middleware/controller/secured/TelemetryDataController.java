package de.agrirouter.middleware.controller.secured;

import de.agrirouter.middleware.api.errorhandling.ParameterValidationException;
import de.agrirouter.middleware.business.DeviceDescriptionService;
import de.agrirouter.middleware.business.DeviceService;
import de.agrirouter.middleware.business.TimeLogService;
import de.agrirouter.middleware.business.dto.timelog.periods.TimeLogPeriods;
import de.agrirouter.middleware.business.dto.timelog.periods.TimeLogPeriodsForDevice;
import de.agrirouter.middleware.business.dto.timelog.periods.TimeLogPeriodsForTeamSet;
import de.agrirouter.middleware.business.parameters.*;
import de.agrirouter.middleware.controller.dto.request.RegisterMachineRequest;
import de.agrirouter.middleware.controller.dto.request.SearchMachinesRequest;
import de.agrirouter.middleware.controller.dto.request.SearchTelemetryDataPeriodsRequest;
import de.agrirouter.middleware.controller.dto.request.SearchTelemetryDataRequest;
import de.agrirouter.middleware.controller.dto.request.messaging.PublishTimeLogDataRequest;
import de.agrirouter.middleware.controller.dto.response.*;
import de.agrirouter.middleware.controller.dto.response.domain.DeviceDto;
import de.agrirouter.middleware.controller.dto.response.domain.timelog.RawTimeLogDataDto;
import de.agrirouter.middleware.controller.dto.response.domain.timelog.TimeLogWithRawDataDto;
import de.agrirouter.middleware.controller.dto.response.domain.timelog.periods.TimeLogPeriodDto;
import de.agrirouter.middleware.controller.dto.response.domain.timelog.periods.TimeLogPeriodDtosForDevice;
import de.agrirouter.middleware.controller.dto.response.domain.timelog.periods.TimeLogPeriodDtosForTeamSet;
import de.agrirouter.middleware.controller.dto.response.domain.timelog.periods.TimeLogPeriodsDto;
import de.agrirouter.middleware.domain.DeviceDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Sending messages to the AR.
 */
@RestController
@RequestMapping(SecuredApiController.API_PREFIX + "/telemetry-data")
@Tag(
        name = "telemetry data management",
        description = "Operations for telemetry data management, i.e. search for telemetry data, search for time log periods or fetch data from the internal storage."
)
public class TelemetryDataController implements SecuredApiController {

    private final DeviceDescriptionService deviceDescriptionService;
    private final DeviceService deviceService;
    private final TimeLogService timeLogService;
    private final ModelMapper modelMapper;

    public TelemetryDataController(DeviceDescriptionService deviceDescriptionService,
                                   DeviceService deviceService,
                                   TimeLogService timeLogService,
                                   ModelMapper modelMapper) {
        this.deviceDescriptionService = deviceDescriptionService;
        this.deviceService = deviceService;
        this.timeLogService = timeLogService;
        this.modelMapper = modelMapper;
    }

    /**
     * Register a machine.
     */
    @PostMapping(
            value = "/register/machine/{externalEndpointId}",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "telemetry-data.register-machine",
            description = "Register a new machine within the middleware and within the agrirouter.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "The response if the machine was created successfully.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            description = "The response from the middleware.",
                                            implementation = RegisterMachineResponse.class
                                    )
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
                            responseCode = "400",
                            description = "In case of a parameter validation exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ParameterValidationProblemResponse.class
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
    public ResponseEntity<RegisterMachineResponse> registerMachine(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId,
                                                                   @Parameter(description = "The request the information to register a machine.", required = true) @Valid @RequestBody RegisterMachineRequest registerMachineRequest,
                                                                   @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var registerMachineParameters = new RegisterMachineParameters();
        registerMachineParameters.setExternalEndpointId(externalEndpointId);
        registerMachineParameters.setBase64EncodedDeviceDescription(registerMachineRequest.getBase64EncodedDeviceDescription());
        registerMachineParameters.setCustomTeamSetContextId(registerMachineRequest.getCustomTeamSetContextId());
        final var teamSetContextId = deviceDescriptionService.registerMachine(registerMachineParameters);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterMachineResponse(teamSetContextId));
    }

    /**
     * Publish a message for a given team set.
     *
     * @param publishTimeLogDataRequest -
     * @return -
     */
    @PostMapping(
            value = "/publish/{externalEndpointId}/{teamSetContextId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "telemetry-data.publish-message",
            description = "Publish a message for a given team set.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Response if the message was published successfully.",
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
                            responseCode = "400",
                            description = "In case of a parameter validation exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ParameterValidationProblemResponse.class
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
    public ResponseEntity<Void> publish(@Parameter(description = "The external endpoint ID.", required = true) @PathVariable String externalEndpointId,
                                        @Parameter(description = "The team set context ID.", required = true) @PathVariable String teamSetContextId,
                                        @Parameter(description = "The request body containing all necessary information to publish a time log.", required = true) @Valid @RequestBody PublishTimeLogDataRequest publishTimeLogDataRequest,
                                        @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        publishTimeLogDataRequest.getBase64EncodedMessages().forEach(base64EncodedTimeLog -> {
                    final var publishTimeLogParameters = new PublishTimeLogParameters();
                    publishTimeLogParameters.setExternalEndpointId(externalEndpointId);
                    publishTimeLogParameters.setTeamSetContextId(teamSetContextId);
                    publishTimeLogParameters.setBase64EncodedTimeLog(base64EncodedTimeLog);
                    timeLogService.publish(publishTimeLogParameters);
                }
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Search machines.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @PostMapping(
            value = "/search/machines",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "telemetry-data.search-machines",
            description = "Search machines within the middleware.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The response, containing all machines found.",
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
                            responseCode = "400",
                            description = "In case of a parameter validation exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ParameterValidationProblemResponse.class
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
    public ResponseEntity<SearchMachineResponse> searchMachines(@Parameter(description = "The request for searching for machines.", required = true) @Valid @RequestBody SearchMachinesRequest searchMachinesRequest,
                                                                @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var searchMachineParameters = new SearchMachinesParameters();
        modelMapper.map(searchMachinesRequest, searchMachineParameters);
        final var devices = deviceService.search(searchMachineParameters);
        final var deviceDtos = devices.stream().map(device -> {
            final var deviceDto = new DeviceDto();
            modelMapper.map(device, deviceDto);
            if (searchMachinesRequest.isWithCurrentDeviceDescription()) {
                deviceDto.setCurrentDeviceDescription(device.getDeviceDescriptions().stream()
                        .min(Comparator.comparingLong(DeviceDescription::getTimestamp)).get().getDocument());
            }
            return deviceDto;
        }).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(new SearchMachineResponse(deviceDtos));
    }

    /**
     * Search for the time periods with the machine data.
     *
     * @param searchTelemetryDataPeriodsRequest -
     * @return -
     */
    @PostMapping(
            value = "/search/time-periods",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "telemetry-data.search-time-periods",
            description = "Search for the time periods with the machine data.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The response containing the time log periods for the devices.",
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
                            responseCode = "400",
                            description = "In case of a parameter validation exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ParameterValidationProblemResponse.class
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
    public ResponseEntity<TimeLogPeriodDtosForDeviceResponse> searchTelemetryDataTimePeriods(@Parameter(description = "The request with all necessary information to search for the time log periods.", required = true) @Valid @RequestBody SearchTelemetryDataPeriodsRequest searchTelemetryDataPeriodsRequest,
                                                                                             @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var searchTimeLogPeriodsParameters = modelMapper.map(searchTelemetryDataPeriodsRequest, SearchTimeLogPeriodsParameters.class);
        final var timeLogPeriodsForDevices = timeLogService.searchTimeLogPeriods(searchTimeLogPeriodsParameters);
        final var timeLogPeriodDtosForDevices = new ArrayList<TimeLogPeriodDtosForDevice>();
        timeLogPeriodsForDevices
                .forEach(timeLogPeriodsForDevice -> mapTimeLogPeriodsForDevices(timeLogPeriodDtosForDevices, timeLogPeriodsForDevice));
        return ResponseEntity.status(HttpStatus.OK).body(new TimeLogPeriodDtosForDeviceResponse(timeLogPeriodsForDevices.size(), timeLogPeriodDtosForDevices));
    }

    private void mapTimeLogPeriodsForDevices(ArrayList<TimeLogPeriodDtosForDevice> timeLogPeriodDtosForDevices, TimeLogPeriodsForDevice timeLogPeriodsForDevice) {
        final var timeLogPeriodDtosForDevice = new TimeLogPeriodDtosForDevice();
        timeLogPeriodDtosForDevice.setDevice(modelMapper.map(timeLogPeriodsForDevice.getDevice(), DeviceDto.class));
        timeLogPeriodDtosForDevice.setTimeLogPeriodsForTeamSet(new ArrayList<>());
        timeLogPeriodsForDevice.getTimeLogPeriodsForTeamSet().forEach(timeLogPeriodsForTeamSet -> mapTimeLogPeriodsForTeamSet(timeLogPeriodDtosForDevice, timeLogPeriodsForTeamSet));

        timeLogPeriodDtosForDevices.add(timeLogPeriodDtosForDevice);
    }

    private void mapTimeLogPeriodsForTeamSet(TimeLogPeriodDtosForDevice timeLogPeriodDtosForDevice, TimeLogPeriodsForTeamSet timeLogPeriodsForTeamSet) {
        final var timeLogPeriodDtosForTeamSet = new TimeLogPeriodDtosForTeamSet();
        timeLogPeriodDtosForTeamSet.setTeamSetContextId(timeLogPeriodsForTeamSet.getTeamSetContextId());
        mapTimeLogPeriods(timeLogPeriodDtosForTeamSet, timeLogPeriodsForTeamSet.getTimeLogPeriods());
        timeLogPeriodDtosForDevice.getTimeLogPeriodsForTeamSet().add(timeLogPeriodDtosForTeamSet);
    }

    private void mapTimeLogPeriods(TimeLogPeriodDtosForTeamSet timeLogPeriodDtosForTeamSet, TimeLogPeriods timeLogPeriods) {
        final var timeLogPeriodsDto = new TimeLogPeriodsDto();
        timeLogPeriodsDto.setTimeLogPeriods(new ArrayList<>());
        timeLogPeriods.getTimeLogPeriods().forEach(timeLogPeriod -> {
            final var timeLogPeriodDto = modelMapper.map(timeLogPeriod, TimeLogPeriodDto.class);
            timeLogPeriodDto.setHumanReadableBegin(Instant.ofEpochSecond(timeLogPeriod.getBegin()));
            timeLogPeriodDto.setHumanReadableEnd(Instant.ofEpochSecond(timeLogPeriod.getEnd()));
            timeLogPeriodsDto.getTimeLogPeriods().add(timeLogPeriodDto);
        });
        timeLogPeriodDtosForTeamSet.setTimeLogPeriods(timeLogPeriodsDto);
    }

    /**
     * Search for the time logs with the machine data and time log period data.
     *
     * @param searchTelemetryDataRequest -
     * @return -
     */
    @PostMapping(
            value = "/search/time-logs",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "telemetry-data.search-time-logs",
            description = "Search for the time logs with the machine data and time log period data.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The response containing all time logs.",
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
                            responseCode = "400",
                            description = "In case of a parameter validation exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ParameterValidationProblemResponse.class
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
    public ResponseEntity<TimeLogSearchResponse> searchTelemetryDataTimeLogs(@Parameter(description = "The request with all necessary information to search for the telemetry data (time logs).", required = true) @Valid @RequestBody SearchTelemetryDataRequest searchTelemetryDataRequest,
                                                                             @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var messagesForTimeLogPeriodParameters = modelMapper.map(searchTelemetryDataRequest, MessagesForTimeLogPeriodParameters.class);
        final var messages = timeLogService.getMessagesForTimeLogPeriod(messagesForTimeLogPeriodParameters);
        final var timeLogsWithRawData = messages.stream().map(timeLog -> modelMapper.map(timeLog, TimeLogWithRawDataDto.class)).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(new TimeLogSearchResponse(timeLogsWithRawData.size(), timeLogsWithRawData));
    }

    /**
     * Search for the time logs with the machine data and time log period data.
     *
     * @param searchTelemetryDataRequest -
     * @return -
     */
    @PostMapping(
            value = "/search/time-logs/raw",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "telemetry-data.search-raw-time-logs",
            description = "Search for the raw time logs with the machine data and time log period data.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The response containing all time logs.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of an business exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ErrorResponse.class
                                    ),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "In case of a parameter validation exception.",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ParameterValidationProblemResponse.class
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
    public ResponseEntity<RawTimeLogSearchResponse> searchTelemetryRawDataTimeLogs(@Parameter(description = "The request with all necessary information to search for the telemetry data (time logs).", required = true) @Valid @RequestBody SearchTelemetryDataRequest searchTelemetryDataRequest,
                                                                                   @Parameter(hidden = true) Errors errors) {
        if (errors.hasErrors()) {
            throw new ParameterValidationException(errors);
        }
        final var messagesForTimeLogPeriodParameters = modelMapper.map(searchTelemetryDataRequest, MessagesForTimeLogPeriodParameters.class);
        final var messages = timeLogService.getMessagesForTimeLogPeriod(messagesForTimeLogPeriodParameters);
        final var rawTimeLogs = messages.stream().map(timeLog -> modelMapper.map(timeLog, RawTimeLogDataDto.class)).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(new RawTimeLogSearchResponse(rawTimeLogs.size(), rawTimeLogs));
    }
}
