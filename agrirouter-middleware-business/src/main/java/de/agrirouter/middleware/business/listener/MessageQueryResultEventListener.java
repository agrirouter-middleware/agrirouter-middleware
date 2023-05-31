package de.agrirouter.middleware.business.listener;

import agrirouter.feed.response.FeedResponse;
import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import com.dke.data.agrirouter.api.enums.ContentMessageType;
import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.dke.data.agrirouter.api.service.messaging.mqtt.MessageQueryService;
import com.dke.data.agrirouter.api.service.parameters.DeleteMessageParameters;
import com.dke.data.agrirouter.api.service.parameters.MessageConfirmationParameters;
import com.dke.data.agrirouter.api.service.parameters.MessageQueryParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.DeleteMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.mqtt.MessageConfirmationServiceImpl;
import com.dke.data.agrirouter.impl.messaging.mqtt.MessageQueryServiceImpl;
import com.google.protobuf.ByteString;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.MessageQueryResultEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.DeviceDescriptionService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.business.TimeLogService;
import de.agrirouter.middleware.business.cache.events.BusinessEvent;
import de.agrirouter.middleware.business.cache.events.BusinessEventApplicationEvent;
import de.agrirouter.middleware.business.cache.events.BusinessEventType;
import de.agrirouter.middleware.business.cache.query.LatestQueryResults;
import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.ContentMessageMetadata;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.taskdata.TaskDataTimeLogContainer;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.isoxml.TaskDataTimeLogService;
import de.agrirouter.middleware.persistence.ContentMessageRepository;
import de.agrirouter.middleware.persistence.TaskDataTimeLogContainerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static de.agrirouter.middleware.api.logging.BusinessOperationLogService.NA;

/**
 * Confirm messages from the AR.
 */
@Slf4j
@Service
public class MessageQueryResultEventListener {

    private final MqttClientManagementService mqttClientManagementService;
    private final EndpointService endpointService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final DecodeMessageService decodeMessageService;
    private final TimeLogService timeLogService;
    private final TaskDataTimeLogContainerRepository taskDataTimeLogContainerRepository;
    private final ContentMessageRepository contentMessageRepository;
    private final TaskDataTimeLogService taskDataTimeLogService;
    private final DeviceDescriptionService deviceDescriptionService;
    private final BusinessOperationLogService businessOperationLogService;
    private final LatestQueryResults latestQueryResults;
    private final ApplicationEventPublisher applicationEventPublisher;

    public MessageQueryResultEventListener(MqttClientManagementService mqttClientManagementService,
                                           EndpointService endpointService,
                                           MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                           DecodeMessageService decodeMessageService,
                                           TimeLogService timeLogService,
                                           TaskDataTimeLogContainerRepository taskDataTimeLogContainerRepository,
                                           ContentMessageRepository contentMessageRepository,
                                           TaskDataTimeLogService taskDataTimeLogService,
                                           DeviceDescriptionService deviceDescriptionService,
                                           BusinessOperationLogService businessOperationLogService,
                                           LatestQueryResults latestQueryResults,
                                           ApplicationEventPublisher applicationEventPublisher) {
        this.mqttClientManagementService = mqttClientManagementService;
        this.endpointService = endpointService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.decodeMessageService = decodeMessageService;
        this.timeLogService = timeLogService;
        this.taskDataTimeLogContainerRepository = taskDataTimeLogContainerRepository;
        this.contentMessageRepository = contentMessageRepository;
        this.taskDataTimeLogService = taskDataTimeLogService;
        this.deviceDescriptionService = deviceDescriptionService;
        this.businessOperationLogService = businessOperationLogService;
        this.latestQueryResults = latestQueryResults;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Confirm existing messages.
     *
     * @param messageQueryResultEvent -
     */
    @EventListener
    public void handleConfirmMessages(MessageQueryResultEvent messageQueryResultEvent) {
        log.debug("Incoming event for message confirmation.");
        saveAndConfirmMessages(messageQueryResultEvent.getFetchMessageResponse());
        businessOperationLogService.log(new EndpointLogInformation(NA, messageQueryResultEvent.getFetchMessageResponse().getSensorAlternateId()), "Confirming pending messages that where not fetched earlier.");
    }

    /**
     * Persist the content message.
     *
     * @param contentMessageMetadata Metadata for the content message.
     * @param receiverId             The ID of the receiver.
     * @param message                The message content itself.
     * @param technicalMessageType   The technical message type.
     */
    private void persistContentMessage(ContentMessageMetadata contentMessageMetadata, String receiverId, ByteString message, String technicalMessageType) {
        try {
            final var contentMessage = new ContentMessage();
            contentMessage.setAgrirouterEndpointId(receiverId);
            contentMessage.setMessageContent(message.toByteArray());
            contentMessage.setContentMessageMetadata(contentMessageMetadata);
            contentMessageRepository.save(contentMessage);

            var endpoint = endpointService.findByAgrirouterEndpointId(receiverId);
            var externalEndpointId = endpoint.getExternalEndpointId();
            applicationEventPublisher.publishEvent(new BusinessEventApplicationEvent(this, externalEndpointId, new BusinessEvent(Instant.now(), BusinessEventType.NON_TELEMETRY_MESSAGE_RECEIVED)));

            if (technicalMessageType.equals(ContentMessageType.ISO_11783_TASKDATA_ZIP.getKey())) {
                final var timeLogs = taskDataTimeLogService.parseMessageContent(contentMessage.getMessageContent());
                taskDataTimeLogContainerRepository.save(new TaskDataTimeLogContainer(contentMessage, timeLogs));
                applicationEventPublisher.publishEvent(new BusinessEventApplicationEvent(this, externalEndpointId, new BusinessEvent(Instant.now(), BusinessEventType.TASK_DATA_RECEIVED)));
            }

            if (technicalMessageType.equals(ContentMessageType.ISO_11783_DEVICE_DESCRIPTION.getKey())) {
                deviceDescriptionService.saveReceivedDeviceDescription(contentMessage);
                applicationEventPublisher.publishEvent(new BusinessEventApplicationEvent(this, externalEndpointId, new BusinessEvent(Instant.now(), BusinessEventType.DEVICE_DESCRIPTION_RECEIVED)));
            }

            if (technicalMessageType.equals(ContentMessageType.ISO_11783_TIME_LOG.getKey())) {
                timeLogService.save(contentMessage);
                applicationEventPublisher.publishEvent(new BusinessEventApplicationEvent(this, externalEndpointId, new BusinessEvent(Instant.now(), BusinessEventType.TIME_LOG_RECEIVED)));
            }
        } catch (BusinessException e) {
            log.error(e.getErrorMessage().asLogMessage());
        }
    }

    /**
     * Confirm pending messages for the endpoint.
     *
     * @param agrirouterEndpointId The ID of the endpoint.
     * @param messageIds           The IDs of the messages to confirm.
     */
    private void confirmMessages(String agrirouterEndpointId, Set<String> messageIds) {
        log.debug("Confirming {} messages for the endpoint '{}'.", messageIds.size(), agrirouterEndpointId);
        log.trace("Message IDs >>> {}", messageIds);
        businessOperationLogService.log(new EndpointLogInformation(NA, agrirouterEndpointId), "Confirm messages with the following IDs >>> {} ", messageIds);
        if (!messageIds.isEmpty()) {
            final var endpoint = endpointService.findByAgrirouterEndpointId(agrirouterEndpointId);
            final var iMqttClient = mqttClientManagementService.get(endpoint);
            if (iMqttClient.isEmpty()) {
                log.error(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.getAgrirouterEndpointId()).asLogMessage());
            } else {
                final var messageConfirmationService = new MessageConfirmationServiceImpl(iMqttClient.get());
                final var messageConfirmationParameters = new MessageConfirmationParameters();
                messageConfirmationParameters.setMessageIds(new ArrayList<>(messageIds));
                messageConfirmationParameters.setOnboardingResponse(endpoint.asOnboardingResponse());
                final var messageId = messageConfirmationService.send(messageConfirmationParameters);

                log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
                MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
                messageWaitingForAcknowledgement.setAgrirouterEndpointId(agrirouterEndpointId);
                messageWaitingForAcknowledgement.setMessageId(messageId);
                messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_FEED_CONFIRM.getKey());
                messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
            }
        } else {
            log.debug("No messages to confirm, therefore skipping confirmation.");
        }
    }

    /**
     * Delete pending messages for the endpoint.
     *
     * @param agrirouterEndpointId The ID of the endpoint.
     * @param messageIds           The IDs of the messages to confirm.
     */
    private void deleteMessages(String agrirouterEndpointId, Set<String> messageIds) {
        log.debug("Delete the messages for the endpoint '{}'.", agrirouterEndpointId);
        log.trace("Message IDs >>> {}", messageIds);
        businessOperationLogService.log(new EndpointLogInformation(NA, agrirouterEndpointId), "Delete messages with the following IDs >>> {} ", messageIds);
        if (!messageIds.isEmpty()) {
            final var endpoint = endpointService.findByAgrirouterEndpointId(agrirouterEndpointId);
            final var iMqttClient = mqttClientManagementService.get(endpoint);
            if (iMqttClient.isEmpty()) {
                log.error(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.getAgrirouterEndpointId()).asLogMessage());
            } else {
                final var deleteMessageService = new DeleteMessageServiceImpl(iMqttClient.get());
                final var deleteMessageParameters = new DeleteMessageParameters();
                deleteMessageParameters.setMessageIds(new ArrayList<>(messageIds));
                deleteMessageParameters.setOnboardingResponse(endpoint.asOnboardingResponse());
                final var messageId = deleteMessageService.send(deleteMessageParameters);

                log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
                MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
                messageWaitingForAcknowledgement.setAgrirouterEndpointId(agrirouterEndpointId);
                messageWaitingForAcknowledgement.setMessageId(messageId);
                messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_FEED_DELETE.getKey());
                messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
            }
        } else {
            log.debug("No messages to delete, therefore skipping method call.");
        }
    }

    /**
     * Confirm pending messages for the endpoint.
     *
     * @param fetchMessageResponse The message response to handle.
     */
    private void saveAndConfirmMessages(FetchMessageResponse fetchMessageResponse) {
        log.debug("Saving and confirming the messages from the query for the endpoint '{}'.", fetchMessageResponse.getSensorAlternateId());
        final var decodedMessageResponse = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());
        final var messageQueryResponse = new MessageQueryServiceImpl(null)
                .decode(decodedMessageResponse.getResponsePayloadWrapper().getDetails().getValue());
        log.debug("There are {} messages for this query.", messageQueryResponse.getQueryMetrics().getTotalMessagesInQuery());
        log.debug("There are {} messages in this response.", messageQueryResponse.getMessagesCount());
        log.debug("This is page {} of {} for the query.", messageQueryResponse.getPage().getNumber(), messageQueryResponse.getPage().getTotal());
        final var messageIds = new HashSet<String>();
        final var agrirouterEndpointId = fetchMessageResponse.getSensorAlternateId();
        messageQueryResponse.getMessagesList().forEach(feedMessage -> {
            saveContentMessage(feedMessage);
            messageIds.add(feedMessage.getHeader().getMessageId());
        });
        try {
            final var endpoint = endpointService.findByAgrirouterEndpointId(fetchMessageResponse.getSensorAlternateId());
            saveLatestQueryResult(endpoint.getExternalEndpointId(), messageQueryResponse);
            confirmMessages(agrirouterEndpointId, messageIds);
            if (messageQueryResponse.getQueryMetrics().getTotalMessagesInQuery() > messageQueryResponse.getQueryMetrics().getMaxCountRestriction()) {
                log.debug("There are {} messages in total, the current count restriction is {}. Sending out another event to fetch the messages.", messageQueryResponse.getQueryMetrics().getTotalMessagesInQuery(), messageQueryResponse.getQueryMetrics().getMaxCountRestriction());
                fetchAndConfirmExistingMessages(endpoint);
            }
        } catch (BusinessException e) {
            log.error(e.getErrorMessage().asLogMessage());
            deleteMessages(fetchMessageResponse.sensorAlternateId, messageIds);
        }
    }

    private void saveLatestQueryResult(String externalEndpointId, FeedResponse.MessageQueryResponse messageQueryResponse) {
        var queryResult = new LatestQueryResults.QueryResult();
        log.debug("There are {} messages for this query.", messageQueryResponse.getQueryMetrics().getTotalMessagesInQuery());
        log.debug("There are {} messages in this response.", messageQueryResponse.getMessagesCount());
        log.debug("This is page {} of {} for the query.", messageQueryResponse.getPage().getNumber(), messageQueryResponse.getPage().getTotal());
        queryResult.setTotalMessagesInQuery(messageQueryResponse.getQueryMetrics().getTotalMessagesInQuery());
        queryResult.setMessagesCount(messageQueryResponse.getMessagesCount());
        queryResult.setPageNumber(messageQueryResponse.getPage().getNumber());
        queryResult.setPageTotal(messageQueryResponse.getPage().getTotal());
        queryResult.setTimestamp(Instant.now());
        messageQueryResponse.getMessagesList().forEach(feedMessage -> {
            var messageDetails = new LatestQueryResults.QueryResult.MessageDetails();
            messageDetails.setMessageId(feedMessage.getHeader().getMessageId());
            messageDetails.setTechnicalMessageType(feedMessage.getHeader().getTechnicalMessageType());
            messageDetails.setFileName(feedMessage.getHeader().getMetadata().getFileName());
            messageDetails.setSenderId(feedMessage.getHeader().getSenderId());
            messageDetails.setSentTimestamp(feedMessage.getHeader().getSentTimestamp());
            messageDetails.setPayloadSize(feedMessage.getHeader().getPayloadSize());
            queryResult.addMessageDetails(messageDetails);
        });
        latestQueryResults.add(externalEndpointId, queryResult);
    }

    private void fetchAndConfirmExistingMessages(Endpoint endpoint) {
        log.debug("Fetching and confirming additional existing messages for endpoint '{}'.", endpoint.getExternalEndpointId());
        final var iMqttClient = mqttClientManagementService.get(endpoint);
        if (iMqttClient.isEmpty()) {
            log.error(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.getAgrirouterEndpointId()).asLogMessage());
        } else {
            MessageQueryService messageQueryService = new MessageQueryServiceImpl(iMqttClient.get());
            final var parameters = new MessageQueryParameters();
            parameters.setSentFromInSeconds(Instant.now().minus(28, ChronoUnit.DAYS).getEpochSecond());
            parameters.setSentToInSeconds(Instant.now().getEpochSecond());
            parameters.setOnboardingResponse(endpoint.asOnboardingResponse());
            final var messageId = messageQueryService.send(parameters);

            log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
            MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
            messageWaitingForAcknowledgement.setAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
            messageWaitingForAcknowledgement.setMessageId(messageId);
            messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_FEED_MESSAGE_QUERY.getKey());
            messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void saveContentMessage(FeedResponse.MessageQueryResponse.FeedMessage feedMessage) {
        final var contentMessageMetadata = new ContentMessageMetadata();
        contentMessageMetadata.setMessageId(feedMessage.getHeader().getMessageId());
        contentMessageMetadata.setTechnicalMessageType(feedMessage.getHeader().getTechnicalMessageType());
        contentMessageMetadata.setTimestamp(feedMessage.getHeader().getSentTimestamp().getSeconds());
        contentMessageMetadata.setReceiverId(feedMessage.getHeader().getReceiverId());
        contentMessageMetadata.setFilename(feedMessage.getHeader().getMetadata().getFileName());
        contentMessageMetadata.setChunkContextId(feedMessage.getHeader().getChunkContext().getContextId());
        contentMessageMetadata.setCurrentChunk(feedMessage.getHeader().getChunkContext().getCurrent());
        contentMessageMetadata.setTotalChunks(feedMessage.getHeader().getChunkContext().getTotal());
        contentMessageMetadata.setTotalChunkSize(feedMessage.getHeader().getChunkContext().getTotalSize());
        contentMessageMetadata.setPayloadSize(feedMessage.getHeader().getPayloadSize());
        contentMessageMetadata.setSenderId(feedMessage.getHeader().getSenderId());
        contentMessageMetadata.setSequenceNumber(feedMessage.getHeader().getSequenceNumber());
        contentMessageMetadata.setTeamSetContextId(feedMessage.getHeader().getTeamSetContextId());
        persistContentMessage(contentMessageMetadata, feedMessage.getHeader().getReceiverId(), feedMessage.getContent().getValue(), feedMessage.getHeader().getTechnicalMessageType());
    }

}
