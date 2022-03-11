package de.agrirouter.middleware.controller.helper;

import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.controller.dto.response.domain.*;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Helper to create the endpoint status.
 */
public class CreateEndpointStatusHelper {

    /**
     * Map the endpoint to the dedicated DTO.
     *
     * @param modelMapper                             -
     * @param endpointService                         -
     * @param applicationService                      -
     * @param messageWaitingForAcknowledgementService -
     * @param endpoint                                -
     * @return -
     */
    public static EndpointWithStatusDto map(ModelMapper modelMapper, EndpointService endpointService, ApplicationService applicationService, MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService, Endpoint endpoint) {
        final var endpointWithStatusDto = new EndpointWithStatusDto();
        modelMapper.map(endpoint, endpointWithStatusDto);
        final var errors = new ArrayList<LogEntryDto>();
        endpointService.getErrors(endpoint).forEach(error -> {
            final var logEntryDto = new LogEntryDto();
            modelMapper.map(error, logEntryDto);
            logEntryDto.setTimestamp(Date.from(Instant.ofEpochSecond(error.getTimestamp())));
            errors.add(logEntryDto);
        });
        endpointWithStatusDto.setErrors(errors);
        final var warnings = new ArrayList<LogEntryDto>();
        endpointService.getWarnings(endpoint).forEach(warning -> {
            final var logEntryDto = new LogEntryDto();
            modelMapper.map(warning, logEntryDto);
            logEntryDto.setTimestamp(Date.from(Instant.ofEpochSecond(warning.getTimestamp())));
            warnings.add(logEntryDto);
        });
        endpointWithStatusDto.setWarnings(warnings);
        final var connectionErrors = new ArrayList<ConnectionErrorDto>();
        endpointService.getConnectionState(endpoint).getConnectionErrors().forEach(connectionError -> {
            final var connectionErrorDto = new ConnectionErrorDto();
            modelMapper.map(connectionError, connectionErrorDto);
            connectionErrors.add(connectionErrorDto);
        });
        endpointWithStatusDto.setConnectionErrors(connectionErrors);
        final var optionalApplication = applicationService.findByEndpoint(endpoint);
        if (optionalApplication.isPresent()) {
            final var application = optionalApplication.get();
            endpointWithStatusDto.setInternalApplicationId(application.getInternalApplicationId());
            endpointWithStatusDto.setApplicationId(application.getApplicationId());
            endpointWithStatusDto.setVersionId(application.getVersionId());
        }
        final var messagesWaitingForAcknowledgement = messageWaitingForAcknowledgementService.findAllForAgrirouterEndpointId(endpoint.getAgrirouterEndpointId())
                .stream()
                .map(messageWaitingForAcknowledgement -> modelMapper.map(messageWaitingForAcknowledgement, MessageWaitingForAcknowledgementDto.class))
                .peek(messageWaitingForAcknowledgementDto -> messageWaitingForAcknowledgementDto.setHumanReadableCreated(Date.from(Instant.ofEpochSecond(messageWaitingForAcknowledgementDto.getCreated()))))
                .collect(Collectors.toList());
        endpointWithStatusDto.setMessagesWaitingForAck(messagesWaitingForAcknowledgement);

        final var messageRecipients = endpoint.getMessageRecipients()
                .stream()
                .map(messageRecipient -> modelMapper.map(messageRecipient, MessageRecipientDto.class))
                .toList();
        endpointWithStatusDto.setMessageRecipients(messageRecipients);
        return endpointWithStatusDto;
    }

}
