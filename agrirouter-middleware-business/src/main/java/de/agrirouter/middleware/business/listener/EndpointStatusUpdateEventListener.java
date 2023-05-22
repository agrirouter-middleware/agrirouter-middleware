package de.agrirouter.middleware.business.listener;

import agrirouter.feed.response.FeedResponse;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.dke.data.agrirouter.impl.messaging.mqtt.MessageHeaderQueryServiceImpl;
import de.agrirouter.middleware.api.events.EndpointStatusUpdateEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.business.cache.query.LatestHeaderQueryResults;
import de.agrirouter.middleware.domain.ConnectionState;
import de.agrirouter.middleware.domain.EndpointStatus;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.EndpointStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service to update the endpoint status.
 */
@Slf4j
@Service
public class EndpointStatusUpdateEventListener {

    private final EndpointService endpointService;
    private final EndpointStatusRepository endpointStatusRepository;
    private final DecodeMessageService decodeMessageService;
    private final MqttClientManagementService mqttClientManagementService;
    private final BusinessOperationLogService businessOperationLogService;
    private final LatestHeaderQueryResults latestHeaderQueryResults;

    public EndpointStatusUpdateEventListener(EndpointService endpointService,
                                             EndpointStatusRepository endpointStatusRepository,
                                             DecodeMessageService decodeMessageService,
                                             MqttClientManagementService mqttClientManagementService,
                                             BusinessOperationLogService businessOperationLogService,
                                             LatestHeaderQueryResults latestHeaderQueryResults) {
        this.endpointService = endpointService;
        this.endpointStatusRepository = endpointStatusRepository;
        this.decodeMessageService = decodeMessageService;
        this.mqttClientManagementService = mqttClientManagementService;
        this.businessOperationLogService = businessOperationLogService;
        this.latestHeaderQueryResults = latestHeaderQueryResults;
    }

    /**
     * Update the status of the endpoint after the query message header result has arrived.
     *
     * @param endpointStatusUpdateEvent -
     */
    @EventListener
    public void updateEndpointStatus(EndpointStatusUpdateEvent endpointStatusUpdateEvent) {
        log.debug("Update the endpoint status for the following AR endpoint '{}'.", endpointStatusUpdateEvent.getAgrirouterEndpointId());
        final var existsByAgrirouterEndpointId = endpointService.existsByAgrirouterEndpointId(endpointStatusUpdateEvent.getAgrirouterEndpointId());
        if (existsByAgrirouterEndpointId) {
            final var endpoint = endpointService.findByAgrirouterEndpointId(endpointStatusUpdateEvent.getAgrirouterEndpointId());
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
                saveLatestHeaderQueryResult(endpoint.getExternalEndpointId(), messageQueryResponse);
                endpoint.getEndpointStatus().setNrOfMessagesWithinTheInbox(messageQueryResponse.getQueryMetrics().getTotalMessagesInQuery());
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Update the number of messages within the inbox.");
            }
            endpoint.getEndpointStatus().getConnectionState().setCached(connectionState.cached());
            endpoint.getEndpointStatus().getConnectionState().setConnected(connectionState.connected());
            endpoint.getEndpointStatus().getConnectionState().setClientId(connectionState.clientId());
            endpointStatusRepository.save(endpoint.getEndpointStatus());
            endpointService.save(endpoint);
            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Endpoint status information was successfully updated.");
        } else {
            log.warn("The endpoint was not found in the database, the message was deleted but not saved.");
        }
    }

    private void saveLatestHeaderQueryResult(String externalEndpointId, FeedResponse.HeaderQueryResponse messageQueryResponse) {
        var queryResult = new LatestHeaderQueryResults.QueryResult();
        log.debug("There are {} messages for this query.", messageQueryResponse.getQueryMetrics().getTotalMessagesInQuery());
        log.debug("This is page {} of {} for the query.", messageQueryResponse.getPage().getNumber(), messageQueryResponse.getPage().getTotal());
        queryResult.setTotalMessagesInQuery(messageQueryResponse.getQueryMetrics().getTotalMessagesInQuery());
        queryResult.setPageNumber(messageQueryResponse.getPage().getNumber());
        queryResult.setPageTotal(messageQueryResponse.getPage().getTotal());
        queryResult.setTimestamp(Instant.now());
        messageQueryResponse.getFeedList().forEach(feed -> feed.getHeadersList().forEach(header -> {
            var messageDetails = new LatestHeaderQueryResults.QueryResult.MessageDetails();
            messageDetails.setMessageId(header.getMessageId());
            messageDetails.setTechnicalMessageType(header.getTechnicalMessageType());
            messageDetails.setFileName(header.getMetadata().getFileName());
            messageDetails.setSenderId(feed.getSenderId());
            messageDetails.setSentTimestamp(header.getSentTimestamp());
            messageDetails.setPayloadSize(header.getPayloadSize());
            queryResult.addMessageDetails(messageDetails);
        }));
        latestHeaderQueryResults.add(externalEndpointId, queryResult);
    }

}
