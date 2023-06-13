package de.agrirouter.middleware.controller.helper;

import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.business.cache.messaging.MessageCache;
import de.agrirouter.middleware.controller.dto.response.domain.*;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper to create the endpoint status.
 */
public class EndpointStatusHelper {

    /**
     * Map the endpoint to the dedicated DTO.
     *
     * @param modelMapper        -
     * @param applicationService -
     * @param endpoint           -
     * @return -
     */
    public static EndpointDto mapEndpointWithApplicationDetails(ModelMapper modelMapper,
                                                                ApplicationService applicationService,
                                                                EndpointService endpointService,
                                                                MessageCache messageCache,
                                                                Endpoint endpoint) {
        final var dto = new EndpointDto();
        modelMapper.map(endpoint, dto);
        final var application = applicationService.findByEndpoint(endpoint);
        dto.setInternalApplicationId(application.getInternalApplicationId());
        dto.setApplicationId(application.getApplicationId());
        dto.setVersionId(application.getVersionId());
        dto.setNrOfMessagesCached(messageCache.countCurrentMessageCacheEntries(endpoint.getAgrirouterEndpointId()));
        dto.setConnectionState(mapTechnicalConnectionState(endpointService, endpoint));
        dto.setVirtualEndpoints(mapVirtualEndpointWithApplicationDetails(modelMapper, application, endpointService, messageCache, endpoint.getConnectedVirtualEndpoints()));
        return dto;
    }

    /**
     * Map the endpoint to the dedicated DTO without next level of children.
     *
     * @param modelMapper      -
     * @param application      -
     * @param virtualEndpoints -
     * @return -
     */
    public static List<EndpointDto> mapVirtualEndpointWithApplicationDetails(ModelMapper modelMapper,
                                                                             Application application,
                                                                             EndpointService endpointService,
                                                                             MessageCache messageCache,
                                                                             List<Endpoint> virtualEndpoints) {
        var endpoints = new ArrayList<EndpointDto>();
        if (null != virtualEndpoints) {
            for (Endpoint virtualEndpoint : virtualEndpoints) {
                final var dto = new EndpointDto();
                modelMapper.map(virtualEndpoint, dto);
                dto.setInternalApplicationId(application.getInternalApplicationId());
                dto.setApplicationId(application.getApplicationId());
                dto.setVersionId(application.getVersionId());
                dto.setNrOfMessagesCached(messageCache.countCurrentMessageCacheEntries(virtualEndpoint.getAgrirouterEndpointId()));
                dto.setConnectionState(mapTechnicalConnectionState(endpointService, virtualEndpoint));
                endpoints.add(dto);
            }
        }
        return endpoints;
    }

    /**
     * Map the endpoint to the dedicated DTO.
     *
     * @param modelMapper     -
     * @param endpointService -
     * @param endpoint        -
     * @return -
     */
    public static EndpointConnectionStatusDto mapConnectionStatus(ModelMapper modelMapper,
                                                                  EndpointService endpointService,
                                                                  Endpoint endpoint) {
        final var dto = new EndpointConnectionStatusDto();
        modelMapper.map(endpoint, dto);
        dto.setConnectionState(mapTechnicalConnectionState(endpointService, endpoint));
        final var connectionErrors = new ArrayList<ConnectionErrorDto>();
        endpointService.getConnectionState(endpoint).connectionErrors().forEach(connectionError -> {
            final var connectionErrorDto = new ConnectionErrorDto();
            modelMapper.map(connectionError, connectionErrorDto);
            connectionErrors.add(connectionErrorDto);
        });
        dto.setConnectionErrors(connectionErrors);
        return dto;
    }

    private static ConnectionStateDto mapTechnicalConnectionState(EndpointService endpointService, Endpoint endpoint) {
        var connectionState = endpointService.getConnectionState(endpoint);

        var connectionStateDto = new ConnectionStateDto();
        connectionStateDto.setConnected(connectionState.connected());
        connectionStateDto.setCached(connectionState.cached());
        connectionStateDto.setClientId(connectionState.clientId());
        connectionStateDto.setSubscriptionSent(connectionState.subscriptionSent());

        return connectionStateDto;
    }

    /**
     * Map the endpoint to the dedicated DTO.
     *
     * @param modelMapper     -
     * @param endpointService -
     * @param endpoint        -
     * @return -
     */
    public static EndpointWarningsDto mapWarnings(ModelMapper modelMapper, EndpointService endpointService, Endpoint endpoint) {
        final var dto = new EndpointWarningsDto();
        modelMapper.map(endpoint, dto);
        final var warnings = new ArrayList<LogEntryDto>();
        endpointService.getWarnings(endpoint).forEach(warning -> {
            final var logEntryDto = new LogEntryDto();
            modelMapper.map(warning, logEntryDto);
            logEntryDto.setTimestamp(Date.from(Instant.ofEpochSecond(warning.getTimestamp())));
            warnings.add(logEntryDto);
        });
        dto.setWarnings(warnings);
        dto.setConnectionState(mapTechnicalConnectionState(endpointService, endpoint));
        return dto;
    }

    /**
     * MAp the endpoint to the dedicated DTO.
     *
     * @param modelMapper     -
     * @param endpointService -
     * @param endpoint        -
     * @return -
     */
    public static EndpointErrorsDto mapErrors(ModelMapper modelMapper, EndpointService endpointService, Endpoint endpoint) {
        final var dto = new EndpointErrorsDto();
        modelMapper.map(endpoint, dto);
        final var errors = new ArrayList<LogEntryDto>();
        endpointService.getErrors(endpoint).forEach(error -> {
            final var logEntryDto = new LogEntryDto();
            modelMapper.map(error, logEntryDto);
            logEntryDto.setTimestamp(Date.from(Instant.ofEpochSecond(error.getTimestamp())));
            errors.add(logEntryDto);
        });
        dto.setErrors(errors);
        dto.setConnectionState(mapTechnicalConnectionState(endpointService, endpoint));
        return dto;
    }

    /**
     * Map the missing acknowledgements.
     *
     * @param modelMapper                             -
     * @param endpointService                         -
     * @param messageWaitingForAcknowledgementService -
     * @param endpoint                                -
     * @return -
     */
    public static MissingAcknowledgementsDto mapMissingAcknowledgements(ModelMapper modelMapper, EndpointService endpointService, MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService, Endpoint endpoint) {
        final var dto = new MissingAcknowledgementsDto();
        modelMapper.map(endpoint, dto);

        final var messagesWaitingForAcknowledgement = messageWaitingForAcknowledgementService.findAllForAgrirouterEndpointId(endpoint.getAgrirouterEndpointId())
                .stream()
                .map(messageWaitingForAcknowledgement -> modelMapper.map(messageWaitingForAcknowledgement, MessageWaitingForAcknowledgementDto.class))
                .peek(messageWaitingForAcknowledgementDto -> messageWaitingForAcknowledgementDto.setHumanReadableCreated(Date.from(Instant.ofEpochSecond(messageWaitingForAcknowledgementDto.getCreated()))))
                .collect(Collectors.toList());
        dto.setMessagesWaitingForAck(messagesWaitingForAcknowledgement);
        dto.setConnectionState(mapTechnicalConnectionState(endpointService, endpoint));
        return dto;
    }

    /**
     * Map the technical connection status.
     *
     * @param modelMapper                 -
     * @param endpointService             -
     * @param mqttClientManagementService -
     * @param endpoint                    -
     * @return -
     */
    public static TechnicalConnectionStateDto mapTechnicalConnectionState(ModelMapper modelMapper, EndpointService endpointService, MqttClientManagementService mqttClientManagementService, Endpoint endpoint) {
        final var dto = new TechnicalConnectionStateDto();
        modelMapper.map(endpoint, dto);
        dto.setTechnicalConnectionState(mqttClientManagementService.getTechnicalState(endpoint));
        dto.setConnectionState(mapTechnicalConnectionState(endpointService, endpoint));
        return dto;
    }
}
