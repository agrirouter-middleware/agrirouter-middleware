package de.agrirouter.middleware.business;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.documents.Field;
import de.agrirouter.middleware.persistence.mongo.FieldRepository;
import efdi.GrpcEfdi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FieldService {

    private final EndpointService endpointService;
    private final FieldRepository fieldRepository;

    public void save(ContentMessage contentMessage) {
        log.debug("Saving field for content message with ID: {}", contentMessage.getId());
        final var endpoint = endpointService.findByAgrirouterEndpointId(contentMessage.getContentMessageMetadata().getReceiverId());
        var field = new Field();
        field.setAgrirouterEndpointId(contentMessage.getAgrirouterEndpointId());
        field.setMessageId(contentMessage.getContentMessageMetadata().getMessageId());
        field.setReceiverId(contentMessage.getContentMessageMetadata().getReceiverId());
        field.setSenderId(contentMessage.getContentMessageMetadata().getSenderId());
        field.setTimestamp(contentMessage.getContentMessageMetadata().getTimestamp());
        field.setExternalEndpointId(endpoint.getExternalEndpointId());
        var optionalDocument = convert(contentMessage.getMessageContent());
        optionalDocument.ifPresent(field::setDocument);
        fieldRepository.save(field);
    }

    /**
     * Convert the given device description into a JSON document.
     *
     * @param messageContent -
     * @return -
     */
    public Optional<Document> convert(byte[] messageContent) {
        try {
            var partField = GrpcEfdi.Partfield.parseFrom(ByteString.copyFrom(messageContent));
            var json = JsonFormat.printer().print(partField);
            var document = Document.parse(json);
            return Optional.ofNullable(document);
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse the PartField message content.", e);
            return Optional.empty();
        }
    }
}
