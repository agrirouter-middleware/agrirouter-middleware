package de.agrirouter.middleware.business;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.documents.Field;
import de.agrirouter.middleware.domain.documents.Notification;
import de.agrirouter.middleware.domain.enums.ChangeType;
import de.agrirouter.middleware.domain.enums.EntityType;
import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import de.agrirouter.middleware.integration.SendMessageIntegrationService;
import de.agrirouter.middleware.integration.parameters.MessagingIntegrationParameters;
import de.agrirouter.middleware.persistence.mongo.FieldRepository;
import de.agrirouter.middleware.persistence.mongo.NotificationRepository;
import efdi.GrpcEfdi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FieldService {

    private final EndpointService endpointService;
    private final FieldRepository fieldRepository;
    private final SendMessageIntegrationService sendMessageIntegrationService;
    private final NotificationRepository notificationRepository;

    /**
     * Save the field.
     *
     * @param contentMessage The content message.
     */
    public void save(ContentMessage contentMessage) {
        log.debug("Saving field for content message with ID: {}", contentMessage.getId());
        final var endpoint = endpointService.findByAgrirouterEndpointId(contentMessage.getContentMessageMetadata().getReceiverId());
        var optionalDocument = convert(contentMessage.getMessageContent());
        if (optionalDocument.isPresent()) {
            var document = optionalDocument.get();
            var extractUris = extractUris(document);
            if (extractUris.isEmpty()) {
                log.warn("No URIs found for field form content message with the ID: {}", contentMessage.getId());
                throw new BusinessException(ErrorMessageFactory.couldNotParseField());
            } else {
                extractUris.sort(String::compareTo);
                extractUris.forEach(uri -> log.debug("Found URI: {}", uri));
                var fieldId = extractUris.get(0);
                this.fieldRepository.findByExternalEndpointIdAndFieldId(endpoint.getExternalEndpointId(), fieldId).ifPresentOrElse(f -> {
                    log.debug("Field with ID {} already exists, therefore updating it.", fieldId);
                    f.setMessageId(contentMessage.getContentMessageMetadata().getMessageId());
                    f.setTimestamp(contentMessage.getContentMessageMetadata().getTimestamp());
                    f.setReceiverId(contentMessage.getContentMessageMetadata().getReceiverId());
                    f.setSenderId(contentMessage.getContentMessageMetadata().getSenderId());
                    f.setDocument(document);
                    fieldRepository.save(f);
                    var notification = new Notification();
                    notification.setCreatedAt(Instant.now());
                    notification.setExternalEndpointId(endpoint.getExternalEndpointId());
                    notification.setChangeType(ChangeType.UPDATED);
                    notification.setEntityType(EntityType.FIELD);
                    notification.setEntityId(fieldId);
                    notificationRepository.save(notification);
                }, () -> {
                    log.debug("Field with ID {} does not exist, therefore saving it.", fieldId);
                    var field = new Field();
                    field.setAgrirouterEndpointId(contentMessage.getAgrirouterEndpointId());
                    field.setMessageId(contentMessage.getContentMessageMetadata().getMessageId());
                    field.setReceiverId(contentMessage.getContentMessageMetadata().getReceiverId());
                    field.setSenderId(contentMessage.getContentMessageMetadata().getSenderId());
                    field.setTimestamp(contentMessage.getContentMessageMetadata().getTimestamp());
                    field.setExternalEndpointId(endpoint.getExternalEndpointId());
                    field.setFieldId(fieldId);
                    field.setDocument(document);
                    fieldRepository.save(field);
                    var notification = new Notification();
                    notification.setCreatedAt(Instant.now());
                    notification.setExternalEndpointId(endpoint.getExternalEndpointId());
                    notification.setChangeType(ChangeType.CREATED);
                    notification.setEntityType(EntityType.FIELD);
                    notification.setEntityId(fieldId);
                    notificationRepository.save(notification);
                    log.debug("Field with ID {} has been created.", fieldId);
                });
            }
        } else {
            log.warn("Could not convert the message content into a JSON document.");
            throw new BusinessException(ErrorMessageFactory.couldNotParseField());
        }
    }

    protected List<String> extractUris(Document document) {
        if (document.containsKey("partfieldId")) {
            var partFieldId = document.get("partfieldId", Document.class);
            if (partFieldId != null && partFieldId.containsKey("uri")) {
                var uri = partFieldId.get("uri");
                if (uri instanceof List<?>) {
                    return ((List<?>) uri).stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .toList();
                } else if (uri instanceof String) {
                    return List.of((String) uri);
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Convert the given message content into a JSON document.
     *
     * @param messageContent -
     * @return -
     */
    private Optional<Document> convert(byte[] messageContent) {
        try {
            var partField = GrpcEfdi.Partfield.parseFrom(ByteString.copyFrom(messageContent));
            var json = JsonFormat.printer().print(partField);
            var document = Document.parse(json);
            return Optional.ofNullable(document);
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse the message content.", e);
            return Optional.empty();
        }
    }

    /**
     * Find all fields for the given external endpoint ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @return The fields.
     */
    public List<Field> findByExternalEndpointId(String externalEndpointId) {
        return fieldRepository.findAllByExternalEndpointId(externalEndpointId);
    }

    /**
     * Publish the field.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param fieldAsJson        The field as JSON.
     */
    public void publishField(String externalEndpointId, String fieldAsJson) {
        var optionalEndpoint = endpointService.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            var endpoint = optionalEndpoint.get();
            var optionalField = parse(fieldAsJson);
            if (optionalField.isPresent()) {
                var field = optionalField.get();
                final var messagingIntegrationParameters = new MessagingIntegrationParameters(endpoint.getExternalEndpointId(),
                        TemporaryContentMessageType.ISO_11783_FIELD,
                        Collections.emptyList(),
                        null,
                        field.toByteString(),
                        null);
                sendMessageIntegrationService.publish(endpoint, messagingIntegrationParameters);
            } else {
                log.warn("Could not parse the field, looks like the data provided is invalid.");
                throw new BusinessException(ErrorMessageFactory.couldNotParseField());
            }
        } else {
            log.warn("Could not find the endpoint with the ID {}.", externalEndpointId);
        }
    }

    private Optional<GrpcEfdi.Partfield> parse(String fieldAsJson) {
        try {
            GrpcEfdi.Partfield.Builder builder = GrpcEfdi.Partfield.newBuilder();
            JsonFormat.parser().merge(fieldAsJson, builder);
            return Optional.of(builder.build());
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse the field, looks like the data provided is invalid.", e);
            throw new BusinessException(ErrorMessageFactory.couldNotParseField());
        }
    }

    /**
     * Find the field for the given external endpoint ID and the field ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param fieldId            The field ID.
     * @return The field.
     */
    public Optional<Field> getField(String externalEndpointId, String fieldId) {
        return fieldRepository.findByExternalEndpointIdAndFieldId(externalEndpointId, fieldId);
    }
}
