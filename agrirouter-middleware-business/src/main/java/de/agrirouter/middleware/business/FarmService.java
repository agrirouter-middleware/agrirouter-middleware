package de.agrirouter.middleware.business;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.documents.Farm;
import de.agrirouter.middleware.persistence.mongo.FarmRepository;
import efdi.GrpcEfdi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FarmService {

    private final EndpointService endpointService;
    private final FarmRepository farmRepository;

    public void save(ContentMessage contentMessage) {
        log.debug("Saving field for content message with ID: {}", contentMessage.getId());
        final var endpoint = endpointService.findByAgrirouterEndpointId(contentMessage.getContentMessageMetadata().getReceiverId());
        var farm = new Farm();
        farm.setAgrirouterEndpointId(contentMessage.getAgrirouterEndpointId());
        farm.setMessageId(contentMessage.getContentMessageMetadata().getMessageId());
        farm.setReceiverId(contentMessage.getContentMessageMetadata().getReceiverId());
        farm.setSenderId(contentMessage.getContentMessageMetadata().getSenderId());
        farm.setTimestamp(contentMessage.getContentMessageMetadata().getTimestamp());
        farm.setExternalEndpointId(endpoint.getExternalEndpointId());
        var optionalDocument = convert(contentMessage.getMessageContent());
        optionalDocument.ifPresent(farm::setDocument);
        farmRepository.save(farm);
    }

    /**
     * Convert the given message content into a JSON document.
     *
     * @param messageContent -
     * @return -
     */
    private Optional<Document> convert(byte[] messageContent) {
        try {
            var partField = GrpcEfdi.Farm.parseFrom(ByteString.copyFrom(messageContent));
            var json = JsonFormat.printer().print(partField);
            var document = Document.parse(json);
            return Optional.ofNullable(document);
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse the message content.", e);
            return Optional.empty();
        }
    }
}
