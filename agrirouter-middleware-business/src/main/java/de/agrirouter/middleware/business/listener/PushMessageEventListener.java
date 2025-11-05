package de.agrirouter.middleware.business.listener;

import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodePushNotificationService;
import com.dke.data.agrirouter.api.service.parameters.MessageConfirmationParameters;
import com.dke.data.agrirouter.impl.messaging.mqtt.MessageConfirmationServiceImpl;
import com.google.protobuf.ByteString;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.PushMessageEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.*;
import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.ContentMessageMetadata;
import de.agrirouter.middleware.domain.documents.TaskDataTimeLogContainer;
import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.mqtt.MqttStatistics;
import de.agrirouter.middleware.isoxml.TaskDataTimeLogService;
import de.agrirouter.middleware.persistence.jpa.ContentMessageRepository;
import de.agrirouter.middleware.persistence.mongo.TaskDataTimeLogContainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static de.agrirouter.middleware.api.logging.BusinessOperationLogService.NA;
import static de.agrirouter.middleware.domain.enums.TemporaryContentMessageType.*;

/**
 * Confirm messages from the AR.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushMessageEventListener {

    private final MqttClientManagementService mqttClientManagementService;
    private final EndpointService endpointService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final DecodePushNotificationService decodePushNotificationService;
    private final TimeLogService timeLogService;
    private final FieldService fieldService;
    private final FarmService farmService;
    private final CustomerService customerService;
    private final TaskDataTimeLogContainerRepository taskDataTimeLogContainerRepository;
    private final DeviceDescriptionService deviceDescriptionService;
    private final ContentMessageRepository contentMessageRepository;
    private final TaskDataTimeLogService taskDataTimeLogService;
    private final BusinessOperationLogService businessOperationLogService;
    private final MqttStatistics mqttStatistics;

    /**
     * Handling the unknown message event.
     *
     * @param pushMessageArrivedEvent -
     */
    @EventListener
    public void pushMessageArrived(PushMessageEvent pushMessageArrivedEvent) {
        log.debug("There has been an push notification that has to be handled.");
        var pushNotification = decodePushNotificationService.decode(pushMessageArrivedEvent.getFetchMessageResponse().getCommand().getMessage());
        final var messageIdsToConfirm = new HashSet<String>();
        final var receiverId = pushNotification.getMessages(0).getHeader().getReceiverId();
        pushNotification.getMessagesList().forEach(feedMessage -> {
            saveContentMessage(feedMessage);
            mqttStatistics.increaseNumberOfContentMessagesReceived(feedMessage.getHeader().getTechnicalMessageType());
            messageIdsToConfirm.add(feedMessage.getHeader().getMessageId());
        });
        confirmMessages(receiverId, messageIdsToConfirm);
        businessOperationLogService.log(new EndpointLogInformation(NA, pushMessageArrivedEvent.getFetchMessageResponse().getSensorAlternateId()), "Confirming push message that has arrived.");
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

        // Do not persist raw data for master data messages; process only in specialized services.
        if (isMasterData(technicalMessageType)) {
            if (technicalMessageType.equals(ISO_11783_FIELD.getKey())) {
                fieldService.save(contentMessage);
            } else if (technicalMessageType.equals(ISO_11783_FARM.getKey())) {
                farmService.save(contentMessage);
            } else if (technicalMessageType.equals(ISO_11783_CUSTOMER.getKey())) {
                customerService.save(contentMessage);
            } else {
                log.warn("Unknown master data type {}.", technicalMessageType);
            }
            businessOperationLogService.log(new EndpointLogInformation(NA, receiverId), "Processed master data message without raw persistence.");
            return;
        }

        // Persist all other non-telemetry content messages as before.
        contentMessageRepository.save(contentMessage);

        if (technicalMessageType.equals(TemporaryContentMessageType.ISO_11783_TASKDATA_ZIP.getKey())) {
            final var timeLogs = taskDataTimeLogService.parseMessageContent(contentMessage.getMessageContent());
            taskDataTimeLogContainerRepository.save(new TaskDataTimeLogContainer(contentMessage, timeLogs));
        }

        if (technicalMessageType.equals(TemporaryContentMessageType.ISO_11783_DEVICE_DESCRIPTION.getKey())) {
            deviceDescriptionService.saveReceivedDeviceDescription(contentMessage);
        }

        if (technicalMessageType.equals(TemporaryContentMessageType.ISO_11783_TIME_LOG.getKey())) {
            timeLogService.save(contentMessage);
        }

        businessOperationLogService.log(new EndpointLogInformation(NA, receiverId), "Save content message.");
    }

    protected boolean isMasterData(String technicalMessageType) {
        return technicalMessageType.equals(ISO_11783_FIELD.getKey())
                || technicalMessageType.equals(ISO_11783_FARM.getKey())
                || technicalMessageType.equals(ISO_11783_CUSTOMER.getKey());
    }

    /**
     * Confirm pending messages for the endpoint.
     *
     * @param endpointId The ID of the endpoint.
     * @param messageIds The IDs of the messages to confirm.
     */
    private void confirmMessages(String endpointId, Set<String> messageIds) {
        log.debug("Confirming the messages for the endpoint '{}'.", endpointId);
        log.trace("Message IDs >>> {}", messageIds);
        if (!messageIds.isEmpty()) {
            final var endpoint = endpointService.findByAgrirouterEndpointId(endpointId);
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
                messageWaitingForAcknowledgement.setAgrirouterEndpointId(endpointId);
                messageWaitingForAcknowledgement.setMessageId(messageId);
                messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_FEED_CONFIRM.getKey());
                messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Confirm content message.");
            }
        } else {
            log.debug("No messages to confirm, therefore skipping confirmation.");
        }
    }

}
