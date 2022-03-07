package de.agrirouter.middleware.business.listener;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodePushNotificationService;
import com.dke.data.agrirouter.api.service.parameters.MessageConfirmationParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.MessageConfirmationServiceImpl;
import com.google.protobuf.ByteString;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.PushMessageEvent;
import de.agrirouter.middleware.business.DeviceDescriptionService;
import de.agrirouter.middleware.business.TimeLogService;
import de.agrirouter.middleware.businesslog.BusinessLogService;
import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.ContentMessageMetadata;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Confirm messages from the AR.
 */
@Service
public class PushMessageEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushMessageEventListener.class);

    private final MqttClientManagementService mqttClientManagementService;
    private final EndpointRepository endpointRepository;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final DecodePushNotificationService decodePushNotificationService;
    private final TimeLogService timeLogService;
    private final TaskDataTimeLogContainerRepository taskDataTimeLogContainerRepository;
    private final DeviceDescriptionService deviceDescriptionService;
    private final ContentMessageRepository contentMessageRepository;
    private final TaskDataTimeLogService taskDataTimeLogService;
    private final BusinessLogService businessLogService;

    public PushMessageEventListener(MqttClientManagementService mqttClientManagementService,
                                    EndpointRepository endpointRepository,
                                    MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                    DecodePushNotificationService decodePushNotificationService,
                                    TimeLogService timeLogService,
                                    TaskDataTimeLogContainerRepository taskDataTimeLogContainerRepository,
                                    DeviceDescriptionService deviceDescriptionService,
                                    ContentMessageRepository contentMessageRepository,
                                    TaskDataTimeLogService taskDataTimeLogService,
                                    BusinessLogService businessLogService) {
        this.mqttClientManagementService = mqttClientManagementService;
        this.endpointRepository = endpointRepository;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.decodePushNotificationService = decodePushNotificationService;
        this.timeLogService = timeLogService;
        this.taskDataTimeLogContainerRepository = taskDataTimeLogContainerRepository;
        this.deviceDescriptionService = deviceDescriptionService;
        this.contentMessageRepository = contentMessageRepository;
        this.taskDataTimeLogService = taskDataTimeLogService;
        this.businessLogService = businessLogService;
    }

    /**
     * Handling the unknown message event.
     *
     * @param pushMessageArrivedEvent -
     */
    @EventListener

    public void pushMessageArrived(PushMessageEvent pushMessageArrivedEvent) {
        LOGGER.debug("There has been an push notification that has to be handled.");
        var pushNotification = decodePushNotificationService.decode(pushMessageArrivedEvent.getFetchMessageResponse().getCommand().getMessage());
        final var messageIdsToConfirm = new HashSet<String>();
        final var receiverId = pushNotification.getMessages(0).getHeader().getReceiverId();
        pushNotification.getMessagesList().forEach(feedMessage -> {
            saveContentMessage(feedMessage);
            messageIdsToConfirm.add(feedMessage.getHeader().getMessageId());
        });
        confirmMessages(receiverId, messageIdsToConfirm);
    }

    @SuppressWarnings("DuplicatedCode")
    private void saveContentMessage(agrirouter.feed.push.notification.PushNotificationOuterClass.PushNotification.FeedMessage feedMessage) {
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

    /**
     * Persist the content message.
     *
     * @param contentMessageMetadata Metadata for the content message.
     * @param receiverId             The ID of the receiver.
     * @param message                The message content itself.
     * @param technicalMessageType   The technical message type.
     */
    private void persistContentMessage(ContentMessageMetadata contentMessageMetadata, String receiverId, ByteString message, String technicalMessageType) {
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
            timeLogService.save(contentMessage
            );
            businessLogService.persistContentMessageInDocumentStorage(receiverId, technicalMessageType);
        }
    }

    /**
     * Confirm pending messages for the endpoint.
     *
     * @param endpointId The ID of the endpoint.
     * @param messageIds The IDs of the messages to confirm.
     */
    private void confirmMessages(String endpointId, Set<String> messageIds) {
        LOGGER.debug("Confirming the messages for the endpoint '{}'.", endpointId);
        LOGGER.trace("Message IDs >>> {}", messageIds);
        if (!messageIds.isEmpty()) {
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

}
