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
import de.agrirouter.middleware.business.DeviceDescriptionService;
import de.agrirouter.middleware.business.TimeLogService;
import de.agrirouter.middleware.businesslog.BusinessLogService;
import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.ContentMessageMetadata;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.taskdata.TaskDataTimeLogContainer;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.isoxml.TaskDataTimeLogService;
import de.agrirouter.middleware.persistence.ContentMessageRepository;
import de.agrirouter.middleware.persistence.EndpointRepository;
import de.agrirouter.middleware.persistence.TaskDataTimeLogContainerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Confirm messages from the AR.
 */
@Service
public class MessageQueryResultEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageQueryResultEventListener.class);

    private final MqttClientManagementService mqttClientManagementService;
    private final EndpointRepository endpointRepository;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final DecodeMessageService decodeMessageService;
    private final TimeLogService timeLogService;
    private final TaskDataTimeLogContainerRepository taskDataTimeLogContainerRepository;
    private final ContentMessageRepository contentMessageRepository;
    private final TaskDataTimeLogService taskDataTimeLogService;
    private final BusinessLogService businessLogService;
    private final DeviceDescriptionService deviceDescriptionService;

    public MessageQueryResultEventListener(MqttClientManagementService mqttClientManagementService,
                                           EndpointRepository endpointRepository,
                                           MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                           DecodeMessageService decodeMessageService,
                                           TimeLogService timeLogService,
                                           TaskDataTimeLogContainerRepository taskDataTimeLogContainerRepository,
                                           ContentMessageRepository contentMessageRepository,
                                           TaskDataTimeLogService taskDataTimeLogService,
                                           BusinessLogService businessLogService,
                                           DeviceDescriptionService deviceDescriptionService) {
        this.mqttClientManagementService = mqttClientManagementService;
        this.endpointRepository = endpointRepository;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.decodeMessageService = decodeMessageService;
        this.timeLogService = timeLogService;
        this.taskDataTimeLogContainerRepository = taskDataTimeLogContainerRepository;
        this.contentMessageRepository = contentMessageRepository;
        this.taskDataTimeLogService = taskDataTimeLogService;
        this.businessLogService = businessLogService;
        this.deviceDescriptionService = deviceDescriptionService;
    }

    /**
     * Confirm existing messages.
     *
     * @param messageQueryResultEvent -
     */
    @EventListener
    public void handleConfirmMessages(MessageQueryResultEvent messageQueryResultEvent) {
        LOGGER.debug("Incoming event for message confirmation.");
        saveAndConfirmMessages(messageQueryResultEvent.getFetchMessageResponse());
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
            businessLogService.persistContentMessage(receiverId, technicalMessageType);

            if (technicalMessageType.equals(ContentMessageType.ISO_11783_TASKDATA_ZIP.getKey())) {
                final var timeLogs = taskDataTimeLogService.parseMessageContent(contentMessage.getMessageContent());
                taskDataTimeLogContainerRepository.save(new TaskDataTimeLogContainer(contentMessage, timeLogs));
                businessLogService.persistContentMessageInDocumentStorage(receiverId, technicalMessageType);
            }

            if (technicalMessageType.equals(ContentMessageType.ISO_11783_DEVICE_DESCRIPTION.getKey())) {
                deviceDescriptionService.saveReceivedDeviceDescription(contentMessage);
                businessLogService.persistContentMessageInDocumentStorage(receiverId, technicalMessageType);
            }

            if (technicalMessageType.equals(ContentMessageType.ISO_11783_TIME_LOG.getKey())) {
                timeLogService.save(contentMessage);
                businessLogService.persistContentMessageInDocumentStorage(receiverId, technicalMessageType);
            }
        } catch (BusinessException e) {
            LOGGER.error("An internal business exception occurred.", e);
        }
    }

    /**
     * Confirm pending messages for the endpoint.
     *
     * @param endpointId The ID of the endpoint.
     * @param messageIds The IDs of the messages to confirm.
     */
    public void confirmMessages(String endpointId, Set<String> messageIds) {
        LOGGER.debug("Confirming {} messages for the endpoint '{}'.", messageIds.size(), endpointId);
        if (!messageIds.isEmpty()) {
            LOGGER.trace("Message IDs >>> {}", messageIds);
            final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(endpointId);
            if (optionalEndpoint.isPresent()) {
                final var endpoint = optionalEndpoint.get();
                final var iMqttClient = mqttClientManagementService.get(endpoint.asOnboardingResponse());
                if (iMqttClient.isEmpty()) {
                    throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.asOnboardingResponse().getSensorAlternateId()));
                }
                final var messageConfirmationService = new MessageConfirmationServiceImpl(iMqttClient.get());
                final var messageConfirmationParameters = new MessageConfirmationParameters();
                messageConfirmationParameters.setMessageIds(new ArrayList<>(messageIds));
                messageConfirmationParameters.setOnboardingResponse(endpoint.asOnboardingResponse());
                final var messageId = messageConfirmationService.send(messageConfirmationParameters);

                LOGGER.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
                MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
                messageWaitingForAcknowledgement.setAgrirouterEndpointId(endpointId);
                messageWaitingForAcknowledgement.setMessageId(messageId);
                messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_FEED_CONFIRM.getKey());
                messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
                businessLogService.confirmMessages(endpoint);
            } else {
                throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
            }
        } else {
            LOGGER.debug("No messages to confirm, therefore skipping confirmation.");
        }
    }

    /**
     * Delete pending messages for the endpoint.
     *
     * @param endpointId The ID of the endpoint.
     * @param messageIds The IDs of the messages to confirm.
     */
    public void deleteMessages(String endpointId, Set<String> messageIds) {
        LOGGER.debug("Delete the messages for the endpoint '{}'.", endpointId);
        LOGGER.trace("Message IDs >>> {}", messageIds);
        if (!messageIds.isEmpty()) {
            final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(endpointId);
            if (optionalEndpoint.isPresent()) {
                final var endpoint = optionalEndpoint.get();
                final var iMqttClient = mqttClientManagementService.get(endpoint.asOnboardingResponse());
                if (iMqttClient.isEmpty()) {
                    throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.asOnboardingResponse().getSensorAlternateId()));
                }
                final var deleteMessageService = new DeleteMessageServiceImpl(iMqttClient.get());
                final var deleteMessageParameters = new DeleteMessageParameters();
                deleteMessageParameters.setMessageIds(new ArrayList<>(messageIds));
                deleteMessageParameters.setOnboardingResponse(endpoint.asOnboardingResponse());
                final var messageId = deleteMessageService.send(deleteMessageParameters);

                LOGGER.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
                MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
                messageWaitingForAcknowledgement.setAgrirouterEndpointId(endpointId);
                messageWaitingForAcknowledgement.setMessageId(messageId);
                messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_FEED_DELETE.getKey());
                messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
                businessLogService.deleteMessages(endpoint);
            } else {
                throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
            }
        } else {
            LOGGER.debug("No messages to confirm, therefore skipping confirmation.");
        }
    }

    /**
     * Confirm pending messages for the endpoint.
     *
     * @param fetchMessageResponse The message response to handle.
     */
    private void saveAndConfirmMessages(FetchMessageResponse fetchMessageResponse) {
        LOGGER.debug("Saving and confirming the messages from the query '{}'.", fetchMessageResponse.getSensorAlternateId());
        final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(fetchMessageResponse.getSensorAlternateId());
        final var decodedMessageResponse = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());
        final var messageQueryResponse = new MessageQueryServiceImpl(null)
                .decode(decodedMessageResponse.getResponsePayloadWrapper().getDetails().getValue());
        final var messageIds = new HashSet<String>();
        final var receiverId = fetchMessageResponse.getSensorAlternateId();
        messageQueryResponse.getMessagesList().forEach(feedMessage -> {
            saveContentMessage(feedMessage);
            messageIds.add(feedMessage.getHeader().getMessageId());
        });
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            confirmMessages(receiverId, messageIds);
            if (messageQueryResponse.getQueryMetrics().getTotalMessagesInQuery() > messageQueryResponse.getQueryMetrics().getMaxCountRestriction()) {
                LOGGER.debug("There are {} messages in total, the current count restriction is {}. Sending out another event to fetch the messages.", messageQueryResponse.getQueryMetrics().getTotalMessagesInQuery(), messageQueryResponse.getQueryMetrics().getMaxCountRestriction());
                fetchAndConfirmExistingMessages(endpoint);
            }
        } else {
            LOGGER.warn("The endpoint was not found in the database, the message was deleted but not saved.");
            deleteMessages(receiverId, messageIds);
        }
    }

    private void fetchAndConfirmExistingMessages(Endpoint endpoint) {
        LOGGER.debug("Fetching and confirming additional existing messages for endpoint '{}'.", endpoint.getExternalEndpointId());
        final var iMqttClient = mqttClientManagementService.get(endpoint.asOnboardingResponse());
        if (iMqttClient.isEmpty()) {
            throw new BusinessException(ErrorMessageFactory.couldNotConnectMqttClient(endpoint.asOnboardingResponse().getSensorAlternateId()));
        }
        MessageQueryService messageQueryService = new MessageQueryServiceImpl(iMqttClient.get());
        final var parameters = new MessageQueryParameters();
        parameters.setSentFromInSeconds(Instant.now().minus(28, ChronoUnit.DAYS).getEpochSecond());
        parameters.setSentToInSeconds(Instant.now().getEpochSecond());
        parameters.setOnboardingResponse(endpoint.asOnboardingResponse());
        final var messageId = messageQueryService.send(parameters);

        LOGGER.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
        MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
        messageWaitingForAcknowledgement.setAgrirouterEndpointId(endpoint.getAgrirouterEndpointId());
        messageWaitingForAcknowledgement.setMessageId(messageId);
        messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_FEED_MESSAGE_QUERY.getKey());
        messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
        businessLogService.fetchAndConfirmExistingMessages(endpoint);
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
