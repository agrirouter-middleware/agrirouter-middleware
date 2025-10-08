package de.agrirouter.middleware.business;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.documents.Customer;
import de.agrirouter.middleware.persistence.mongo.CustomerRepository;
import efdi.GrpcEfdi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final EndpointService endpointService;
    private final CustomerRepository customerRepository;

    public void save(ContentMessage contentMessage) {
        log.debug("Saving customer for content message with ID: {}", contentMessage.getId());
        final var endpoint = endpointService.findByAgrirouterEndpointId(contentMessage.getContentMessageMetadata().getReceiverId());
        var customer = new Customer();
        customer.setAgrirouterEndpointId(contentMessage.getAgrirouterEndpointId());
        customer.setMessageId(contentMessage.getContentMessageMetadata().getMessageId());
        customer.setReceiverId(contentMessage.getContentMessageMetadata().getReceiverId());
        customer.setSenderId(contentMessage.getContentMessageMetadata().getSenderId());
        customer.setTimestamp(contentMessage.getContentMessageMetadata().getTimestamp());
        customer.setExternalEndpointId(endpoint.getExternalEndpointId());
        var optionalDocument = convert(contentMessage.getMessageContent());
        optionalDocument.ifPresent(customer::setDocument);
        customerRepository.save(customer);
    }

    /**
     * Convert the given device description into a JSON document.
     *
     * @param messageContent -
     * @return -
     */
    private Optional<Document> convert(byte[] messageContent) {
        try {
            var partField = GrpcEfdi.Customer.parseFrom(ByteString.copyFrom(messageContent));
            var json = JsonFormat.printer().print(partField);
            var document = Document.parse(json);
            return Optional.ofNullable(document);
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse the message content.", e);
            return Optional.empty();
        }
    }
}
