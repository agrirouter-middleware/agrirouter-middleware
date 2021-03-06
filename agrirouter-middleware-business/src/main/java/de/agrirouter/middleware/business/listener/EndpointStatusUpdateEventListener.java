package de.agrirouter.middleware.business.listener;

import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.dke.data.agrirouter.impl.messaging.mqtt.MessageHeaderQueryServiceImpl;
import de.agrirouter.middleware.api.events.EndpointStatusUpdateEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.domain.ConnectionState;
import de.agrirouter.middleware.domain.EndpointStatus;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.EndpointRepository;
import de.agrirouter.middleware.persistence.EndpointStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service to update the endpoint status.
 */
@Slf4j
@Service
public class EndpointStatusUpdateEventListener {

    private final EndpointRepository endpointRepository;
    private final EndpointStatusRepository endpointStatusRepository;
    private final DecodeMessageService decodeMessageService;
    private final MqttClientManagementService mqttClientManagementService;
    private final BusinessOperationLogService businessOperationLogService;

    public EndpointStatusUpdateEventListener(EndpointRepository endpointRepository,
                                             EndpointStatusRepository endpointStatusRepository,
                                             DecodeMessageService decodeMessageService,
                                             MqttClientManagementService mqttClientManagementService,
                                             BusinessOperationLogService businessOperationLogService) {
        this.endpointRepository = endpointRepository;
        this.endpointStatusRepository = endpointStatusRepository;
        this.decodeMessageService = decodeMessageService;
        this.mqttClientManagementService = mqttClientManagementService;
        this.businessOperationLogService = businessOperationLogService;
    }

    /**
     * Update the status of the endpoint after the query message header result has arrived.
     *
     * @param endpointStatusUpdateEvent -
     */
    @EventListener
    public void updateEndpointStatus(EndpointStatusUpdateEvent endpointStatusUpdateEvent) {
        log.debug("Update the endpoint status for the following AR endpoint '{}'.", endpointStatusUpdateEvent.getAgrirouterEndpointId());
        final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(endpointStatusUpdateEvent.getAgrirouterEndpointId());
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            final var connectionState = mqttClientManagementService.getState(endpoint.asOnboardingResponse());
            if (null == endpoint.getEndpointStatus()) {
                endpoint.setEndpointStatus(new EndpointStatus());
                endpoint.getEndpointStatus().setConnectionState(new ConnectionState());
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Adding new status information for the endpoint, there was no status.");
            }
            if (null == endpoint.getEndpointStatus().getConnectionState()) {
                endpoint.getEndpointStatus().setConnectionState(new ConnectionState());
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Adding new connection state for the endpoint, there was no current state.");
            }
            if (null != endpointStatusUpdateEvent.getFetchMessageResponse()) {
                final var fetchMessageResponse = endpointStatusUpdateEvent.getFetchMessageResponse();
                final var decodedMessageResponse = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());
                final var messageQueryResponse = new MessageHeaderQueryServiceImpl(null).decode(decodedMessageResponse.getResponsePayloadWrapper().getDetails().getValue());
                endpoint.getEndpointStatus().setNrOfMessagesWithinTheInbox(messageQueryResponse.getQueryMetrics().getTotalMessagesInQuery());
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Update the number of messages within the inbox.");
            }
            endpoint.getEndpointStatus().getConnectionState().setCached(connectionState.isCached());
            endpoint.getEndpointStatus().getConnectionState().setConnected(connectionState.isConnected());
            endpoint.getEndpointStatus().getConnectionState().setClientId(connectionState.getClientId());
            endpointStatusRepository.save(endpoint.getEndpointStatus());
            endpointRepository.save(endpoint);
            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Endpoint status information was successfully updated.");
        } else {
            log.warn("The endpoint was not found in the database, the message was deleted but not saved.");
        }
    }

}
