package de.agrirouter.middleware.business;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.documents.Customer;
import de.agrirouter.middleware.domain.enums.EntityType;
import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import de.agrirouter.middleware.integration.SendMessageIntegrationService;
import de.agrirouter.middleware.integration.parameters.MessagingIntegrationParameters;
import de.agrirouter.middleware.persistence.mongo.CustomerRepository;
import efdi.GrpcEfdi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final EndpointService endpointService;
    private final SendMessageIntegrationService sendMessageIntegrationService;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;

    /**
     * Save the customer within the local database.
     *
     * @param contentMessage -
     */
    public void save(ContentMessage contentMessage) {
        log.debug("Saving customer for content message with ID: {}", contentMessage.getId());
        final var endpoint = endpointService.findByAgrirouterEndpointId(contentMessage.getContentMessageMetadata().getReceiverId());
        var optionalDocument = convert(contentMessage.getMessageContent());
        if (optionalDocument.isPresent()) {
            var document = optionalDocument.get();
            var extractUris = new ArrayList<>(extractCustomerIds(document));
            if (extractUris.isEmpty()) {
                log.warn("No customer IDs found for customer form content message with the ID: {}", contentMessage.getId());
                throw new BusinessException(ErrorMessageFactory.couldNotParseCustomer());
            } else {
                extractUris.sort(String::compareTo);
                extractUris.forEach(uri -> log.debug("Found customer ID: {}", uri));
                var customerId = extractUris.get(0);
                this.customerRepository.findByExternalEndpointIdAndCustomerId(endpoint.getExternalEndpointId(), customerId).ifPresentOrElse(f -> {
                    log.debug("Customer with ID {} already exists, therefore updating it.", customerId);
                    f.setMessageId(contentMessage.getContentMessageMetadata().getMessageId());
                    f.setTimestamp(contentMessage.getContentMessageMetadata().getTimestamp());
                    f.setReceiverId(contentMessage.getContentMessageMetadata().getReceiverId());
                    f.setSenderId(contentMessage.getContentMessageMetadata().getSenderId());
                    f.setDocument(document);
                    customerRepository.save(f);
                    notificationService.updated(endpoint.getExternalEndpointId(), EntityType.CUSTOMER);
                }, () -> {
                    var customer = new Customer();
                    customer.setExternalEndpointId(endpoint.getExternalEndpointId());
                    customer.setCustomerId(customerId);
                    customer.setMessageId(contentMessage.getContentMessageMetadata().getMessageId());
                    customer.setTimestamp(contentMessage.getContentMessageMetadata().getTimestamp());
                    customer.setReceiverId(contentMessage.getContentMessageMetadata().getReceiverId());
                    customer.setSenderId(contentMessage.getContentMessageMetadata().getSenderId());
                    customer.setDocument(document);
                    customerRepository.save(customer);
                    notificationService.created(endpoint.getExternalEndpointId(), EntityType.CUSTOMER);
                });
            }
        } else {
            log.warn("Could not parse the message content.");
            throw new BusinessException(ErrorMessageFactory.couldNotParseCustomer());
        }
    }

    protected List<String> extractCustomerIds(Document document) {
        if (document.containsKey("customerId")) {
            var customerId = document.get("customerId", Document.class);
            if (customerId != null && customerId.containsKey("uri")) {
                var uri = customerId.get("uri");
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
            var customer = GrpcEfdi.Customer.parseFrom(ByteString.copyFrom(messageContent));
            var json = JsonFormat.printer().print(customer);
            var document = Document.parse(json);
            return Optional.ofNullable(document);
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse the message content.", e);
            return Optional.empty();
        }
    }

    /**
     * Find all customers for the given external endpoint ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @return The customers.
     */
    public List<Customer> findByExternalEndpointId(String externalEndpointId) {
        return customerRepository.findByExternalEndpointId(externalEndpointId);
    }

    /**
     * Sending customer data to the agrirouter.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param customerAsJson     The customer as JSON.
     */
    public void publishCustomer(String externalEndpointId, String customerAsJson) {
        var optionalEndpoint = endpointService.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            var endpoint = optionalEndpoint.get();
            var optionalCustomer = parse(customerAsJson);
            if (optionalCustomer.isPresent()) {
                var customer = optionalCustomer.get();
                final var messagingIntegrationParameters = new MessagingIntegrationParameters(endpoint.getExternalEndpointId(),
                        TemporaryContentMessageType.ISO_11783_CUSTOMER,
                        Collections.emptyList(),
                        null,
                        customer.toByteString(),
                        null);
                sendMessageIntegrationService.publish(endpoint, messagingIntegrationParameters);
            } else {
                log.warn("Could not parse the customer, looks like the data provided is invalid.");
                throw new BusinessException(ErrorMessageFactory.couldNotParseCustomer());
            }
        } else {
            log.warn("Could not find the endpoint with the ID {}.", externalEndpointId);
        }
    }

    private Optional<GrpcEfdi.Customer> parse(String customerAsJson) {
        try {
            GrpcEfdi.Customer.Builder builder = GrpcEfdi.Customer.newBuilder();
            JsonFormat.parser().merge(customerAsJson, builder);
            return Optional.of(builder.build());
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse the customer, looks like the data provided is invalid.", e);
            throw new BusinessException(ErrorMessageFactory.couldNotParseCustomer());
        }
    }

    /**
     * Find the customer for the given external endpoint ID and the customer ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param customerId         The customer ID.
     * @return The customer.
     */
    public Optional<Customer> getCustomer(String externalEndpointId, String customerId) {
        return customerRepository.findByExternalEndpointIdAndCustomerId(externalEndpointId, customerId);
    }
}
