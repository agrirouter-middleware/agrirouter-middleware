package de.agrirouter.middleware.business;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.documents.Farm;
import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import de.agrirouter.middleware.integration.SendMessageIntegrationService;
import de.agrirouter.middleware.integration.parameters.MessagingIntegrationParameters;
import de.agrirouter.middleware.persistence.mongo.FarmRepository;
import efdi.GrpcEfdi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FarmService {

    private final EndpointService endpointService;
    private final FarmRepository farmRepository;
    private final SendMessageIntegrationService sendMessageIntegrationService;

    /**
     * Save the farm.
     *
     * @param contentMessage The content message.
     */
    public void save(ContentMessage contentMessage) {
        log.debug("Saving farm for content message with ID: {}", contentMessage.getId());
        final var endpoint = endpointService.findByAgrirouterEndpointId(contentMessage.getContentMessageMetadata().getReceiverId());
        var optionalDocument = convert(contentMessage.getMessageContent());
        if (optionalDocument.isPresent()) {
            var document = optionalDocument.get();
            var farmId = extractFarmId(document);
            this.farmRepository.findByExternalEndpointIdAndFarmId(endpoint.getExternalEndpointId(), farmId).ifPresentOrElse(f -> {
                log.debug("Farm with ID {} already exists, therefore updating it.", farmId);
                f.setDocument(document);
                farmRepository.save(f);
            }, () -> {
                log.debug("Farm with ID {} does not exist, therefore saving it.", farmId);
                var farm = new Farm();
                farm.setAgrirouterEndpointId(contentMessage.getAgrirouterEndpointId());
                farm.setMessageId(contentMessage.getContentMessageMetadata().getMessageId());
                farm.setReceiverId(contentMessage.getContentMessageMetadata().getReceiverId());
                farm.setSenderId(contentMessage.getContentMessageMetadata().getSenderId());
                farm.setTimestamp(contentMessage.getContentMessageMetadata().getTimestamp());
                farm.setExternalEndpointId(endpoint.getExternalEndpointId());
                farm.setFarmId(farmId);
                farm.setDocument(document);
                farmRepository.save(farm);
            });
        } else {
            log.warn("Could not convert the message content into a JSON document.");
        }
    }

    private String extractFarmId(Document document) {
        if (document.containsKey("farmId")) {
            return document.getString("farmId");
        }
        return null;
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

    /**
     * Find all farms for the given external endpoint ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @return The farms.
     */
    public List<Farm> findByExternalEndpointId(String externalEndpointId) {
        return farmRepository.findByExternalEndpointId(externalEndpointId);
    }

    /**
     * Publish the farm.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param farmAsJson         The farm as JSON.
     */
    public void publishFarm(String externalEndpointId, String farmAsJson) {
        var optionalEndpoint = endpointService.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            var endpoint = optionalEndpoint.get();
            var optionalFarm = parse(farmAsJson);
            if (optionalFarm.isPresent()) {
                var farm = optionalFarm.get();
                final var messagingIntegrationParameters = new MessagingIntegrationParameters(endpoint.getExternalEndpointId(),
                        TemporaryContentMessageType.ISO_11783_FARM,
                        Collections.emptyList(),
                        null,
                        farm.toByteString(),
                        null);
                sendMessageIntegrationService.publish(endpoint, messagingIntegrationParameters);
            } else {
                log.warn("Could not parse the farm, looks like the data provided is invalid.");
                throw new BusinessException(ErrorMessageFactory.couldNotParseFarm());
            }
        } else {
            log.warn("Could not find the endpoint with the ID {}.", externalEndpointId);
        }
    }

    private Optional<GrpcEfdi.Farm> parse(String farmAsJson) {
        try {
            GrpcEfdi.Farm.Builder builder = GrpcEfdi.Farm.newBuilder();
            JsonFormat.parser().merge(farmAsJson, builder);
            return Optional.of(builder.build());
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not parse the farm, looks like the data provided is invalid.", e);
            throw new BusinessException(ErrorMessageFactory.couldNotParseFarm());
        }
    }

    /**
     * Get the farm by the external endpoint ID and the farm ID.
     *
     * @param externalEndpointId The external endpoint ID.
     * @param farmId             The farm ID.
     * @return The farm.
     */
    public Optional<Farm> getFarm(String externalEndpointId, String farmId) {
        return farmRepository.findByExternalEndpointIdAndFarmId(externalEndpointId, farmId);
    }
}
