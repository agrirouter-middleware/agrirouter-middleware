package de.agrirouter.middleware.business.listener;

import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.dke.data.agrirouter.impl.messaging.mqtt.MessageHeaderQueryServiceImpl;
import de.agrirouter.middleware.api.events.EndpointStatusUpdateEvent;
import de.agrirouter.middleware.domain.ConnectionState;
import de.agrirouter.middleware.domain.EndpointStatus;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.EndpointRepository;
import de.agrirouter.middleware.persistence.EndpointStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service to update the endpoint status.
 */
@Service
public class EndpointStatusUpdateEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointStatusUpdateEventListener.class);

    private final EndpointRepository endpointRepository;
    private final EndpointStatusRepository endpointStatusRepository;
    private final DecodeMessageService decodeMessageService;
    private final MqttClientManagementService mqttClientManagementService;

    public EndpointStatusUpdateEventListener(EndpointRepository endpointRepository,
                                             EndpointStatusRepository endpointStatusRepository,
                                             DecodeMessageService decodeMessageService,
                                             MqttClientManagementService mqttClientManagementService) {
        this.endpointRepository = endpointRepository;
        this.endpointStatusRepository = endpointStatusRepository;
        this.decodeMessageService = decodeMessageService;
        this.mqttClientManagementService = mqttClientManagementService;
    }

    /**
     * Update the status of the endpoint after the query message header result has arrived.
     *
     * @param endpointStatusUpdateEvent -
     */
    @EventListener
    public void updateEndpointStatus(EndpointStatusUpdateEvent endpointStatusUpdateEvent) {
        LOGGER.debug("Saving and confirming the messages from the query '{}'.", endpointStatusUpdateEvent.getAgrirouterEndpointId());
        final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(endpointStatusUpdateEvent.getAgrirouterEndpointId());
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            final var connectionState = mqttClientManagementService.getState(endpoint.asOnboardingResponse());
            if (null == endpoint.getEndpointStatus()) {
                endpoint.setEndpointStatus(new EndpointStatus());
                endpoint.getEndpointStatus().setConnectionState(new ConnectionState());
            }
            if (null == endpoint.getEndpointStatus().getConnectionState()) {
                endpoint.getEndpointStatus().setConnectionState(new ConnectionState());
            }
            if (null != endpointStatusUpdateEvent.getFetchMessageResponse()) {
                final var fetchMessageResponse = endpointStatusUpdateEvent.getFetchMessageResponse();
                final var decodedMessageResponse = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());
                final var messageQueryResponse = new MessageHeaderQueryServiceImpl(null).decode(decodedMessageResponse.getResponsePayloadWrapper().getDetails().getValue());
                endpoint.getEndpointStatus().setNrOfMessagesWithinTheInbox(messageQueryResponse.getQueryMetrics().getTotalMessagesInQuery());
            }
            endpoint.getEndpointStatus().getConnectionState().setCached(connectionState.isCached());
            endpoint.getEndpointStatus().getConnectionState().setConnected(connectionState.isConnected());
            endpoint.getEndpointStatus().getConnectionState().setClientId(connectionState.getClientId());
            endpointStatusRepository.save(endpoint.getEndpointStatus());
            endpointRepository.save(endpoint);
        } else {
            LOGGER.warn("The endpoint was not found in the database, the message was deleted but not saved.");
        }
    }

}
