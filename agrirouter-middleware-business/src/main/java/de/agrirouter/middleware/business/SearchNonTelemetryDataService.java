package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.business.dto.MessageStatistics;
import de.agrirouter.middleware.business.parameters.SearchNonTelemetryDataParameters;
import de.agrirouter.middleware.domain.ContentMessageMetadata;
import de.agrirouter.middleware.persistence.ContentMessageRepository;
import de.agrirouter.middleware.persistence.EndpointRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Service to handle business operations round about searching non telemetry data.
 */
@Slf4j
@Service
public class SearchNonTelemetryDataService {

    private final ContentMessageRepository contentMessageRepository;
    private final EndpointRepository endpointRepository;

    public SearchNonTelemetryDataService(ContentMessageRepository contentMessageRepository,
                                         EndpointRepository endpointRepository) {
        this.contentMessageRepository = contentMessageRepository;
        this.endpointRepository = endpointRepository;
    }

    /**
     * Search for non telemetry data.
     *
     * @param searchNonTelemetryDataParameters -
     * @return -
     */
    public List<ContentMessageMetadata> search(SearchNonTelemetryDataParameters searchNonTelemetryDataParameters) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointId(searchNonTelemetryDataParameters.getExternalEndpointId());
        if (optionalEndpoint.isPresent()) {
            log.debug("Searching for existing messages, since the endpoint was found.");
            log.trace("Filter criteria are >>> {}.", searchNonTelemetryDataParameters);
            final List<ContentMessageMetadata> contentMessageMetadata;
            if (null != searchNonTelemetryDataParameters.getTechnicalMessageTypes() && !searchNonTelemetryDataParameters.getTechnicalMessageTypes().isEmpty()) {
                contentMessageMetadata = contentMessageRepository.findMetadata(optionalEndpoint.get().getAgrirouterEndpointId(),
                        searchNonTelemetryDataParameters.getTechnicalMessageTypes().stream().map(ContentMessageType::getKey).toList(),
                        List.of(ContentMessageType.ISO_11783_TIME_LOG.getKey(), ContentMessageType.ISO_11783_DEVICE_DESCRIPTION.getKey()),
                        searchNonTelemetryDataParameters.getSendFrom(),
                        searchNonTelemetryDataParameters.getSendTo());
                log.debug("Found {} content messages in total.", contentMessageMetadata.size());
            } else {
                contentMessageMetadata = contentMessageRepository.findMetadata(optionalEndpoint.get().getAgrirouterEndpointId(),
                        List.of(ContentMessageType.ISO_11783_TIME_LOG.getKey(), ContentMessageType.ISO_11783_DEVICE_DESCRIPTION.getKey()),
                        searchNonTelemetryDataParameters.getSendFrom(),
                        searchNonTelemetryDataParameters.getSendTo());
            }
            var flattenedContentMessageMetadata = flattenContentMessageMetadata(contentMessageMetadata);
            log.debug("The {} content messages are flattened to {} 'real' messages.", contentMessageMetadata.size(), flattenedContentMessageMetadata);
            return flattenedContentMessageMetadata;
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }

    private List<ContentMessageMetadata> flattenContentMessageMetadata(List<ContentMessageMetadata> contentMessageMetadata) {
        final var flattenedContentMessageMetadata = new ArrayList<ContentMessageMetadata>();
        final var alreadyProcessedChunkContextIds = new HashSet<String>();
        final var alreadyProcessedMessageIds = new HashSet<String>();
        contentMessageMetadata.forEach(cmm -> {
            if (alreadyProcessedMessageIds.contains(cmm.getMessageId())) {
                log.debug("The message ID '{}' was already processed, therefore skipping the message.", cmm.getMessageId());
            } else {
                if (StringUtils.isBlank(cmm.getChunkContextId())) {
                    log.debug("This is a single message without a chunking context, therefore we put this one directly into the result.");
                    flattenedContentMessageMetadata.add(cmm);
                } else {
                    if (alreadyProcessedChunkContextIds.contains(cmm.getChunkContextId())) {
                        log.debug("Skipping already existing content message metadata.");
                    } else {
                        alreadyProcessedChunkContextIds.add(cmm.getChunkContextId());
                        flattenedContentMessageMetadata.add(cmm);
                    }
                }
                alreadyProcessedMessageIds.add(cmm.getMessageId());
            }
        });
        return flattenedContentMessageMetadata;
    }

    /**
     * Download the file as byte array. The base64 encoded message content will be decoded.
     *
     * @param externalEndpointId -
     * @param messageId          -
     * @return -
     */
    public byte[] downloadAsByteArray(String externalEndpointId, String messageId) {
        return download(externalEndpointId, messageId);
    }

    private byte[] download(String externalEndpointId, String messageId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var optionalContentMessage = contentMessageRepository.findFirstByAgrirouterEndpointIdAndContentMessageMetadataMessageId(optionalEndpoint.get().getAgrirouterEndpointId(), messageId);
            if (optionalContentMessage.isPresent()) {
                final var contentMessage = optionalContentMessage.get();
                if (contentMessage.getContentMessageMetadata().getTotalChunks() > 1) {
                    log.debug("Looks like we have multiple chunks for the content message. Assembling the message content first. There are {} chunks in total.", contentMessage.getContentMessageMetadata().getTotalChunks());
                    return assembleChunkedMessageContent(optionalEndpoint.get().getAgrirouterEndpointId(), contentMessage.getContentMessageMetadata().getChunkContextId());
                } else {
                    log.debug("This is a single message, therefore returning the content 'as it is'.");
                    return Base64.getDecoder().decode(contentMessage.getMessageContent());
                }
            } else {
                throw new BusinessException(ErrorMessageFactory.couldNotFindContentMessage());
            }
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }

    private byte[] assembleChunkedMessageContent(String agrirouterEndpointId, String chunkContextId) {
        if (StringUtils.isBlank(chunkContextId)) {
            throw new BusinessException(ErrorMessageFactory.couldNotAssembleChunks());
        } else {
            final var contentMessages = contentMessageRepository.findByAgrirouterEndpointIdAndContentMessageMetadataChunkContextId(agrirouterEndpointId, chunkContextId);
            try (var stream = new ByteArrayOutputStream()) {
                contentMessages.stream()
                        .sorted(Comparator.comparingLong(o -> o.getContentMessageMetadata().getCurrentChunk()))
                        .map(cm -> Base64.getDecoder().decode(cm.getMessageContent()))
                        .forEach(mc -> {
                            try {
                                stream.write(mc);
                            } catch (IOException e) {
                                throw new BusinessException(ErrorMessageFactory.couldNotAssembleChunks());
                            }
                        });
                return stream.toByteArray();
            } catch (Exception e) {
                throw new BusinessException(ErrorMessageFactory.couldNotAssembleChunks());
            }
        }
    }

    /**
     * Fetch the message statistics for the endpoint.
     *
     * @param externalEndpointId The external ID of the endpoint.
     * @return The statistics.
     */
    public MessageStatistics getMessageStatistics(String externalEndpointId) {
        var messageStatistics = new MessageStatistics();
        messageStatistics.setExternalEndpointId(externalEndpointId);
        final var optionalEndpoint = endpointRepository.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            var messageCountForTechnicalMessageTypes = contentMessageRepository.countMessagesGroupedByTechnicalMessageType(optionalEndpoint.get().getAgrirouterEndpointId());
            messageCountForTechnicalMessageTypes.forEach(messageCountForTechnicalMessageType -> {
                        log.debug("Found {} messages for technical message type {} for the sender {}.",
                                messageCountForTechnicalMessageType.getNumberOfMessages(),
                                messageCountForTechnicalMessageType.getTechnicalMessageType(),
                                messageCountForTechnicalMessageType.getSenderId());
                        messageStatistics.addMessageStatisticEntry(messageCountForTechnicalMessageType.getSenderId(),
                                new MessageStatistics.MessageStatistic.Entry(messageCountForTechnicalMessageType.getTechnicalMessageType(), messageCountForTechnicalMessageType.getNumberOfMessages()));
                    }
            );
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
        return messageStatistics;
    }

    /**
     * Delete the message with all existing chunks.
     *
     * @param externalEndpointId The external ID of the endpoint.
     * @param messageId          The ID of the message.
     */
    public void delete(String externalEndpointId, String messageId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var optionalContentMessage = contentMessageRepository.findFirstByAgrirouterEndpointIdAndContentMessageMetadataMessageId(optionalEndpoint.get().getAgrirouterEndpointId(), messageId);
            if (optionalContentMessage.isPresent()) {
                final var contentMessage = optionalContentMessage.get();
                if (contentMessage.getContentMessageMetadata().getTotalChunks() > 1) {
                    log.debug("Looks like we have multiple chunks for the content message. Assembling the message content first. There are {} chunks in total.", contentMessage.getContentMessageMetadata().getTotalChunks());
                    deleteChunkedMessageContent(optionalEndpoint.get().getAgrirouterEndpointId(), contentMessage.getContentMessageMetadata().getChunkContextId());
                } else {
                    log.debug("This is a single message, therefore nothing else to do.");
                    var i = contentMessageRepository.deleteByAgrirouterEndpointIdAndContentMessageMetadataMessageId(optionalEndpoint.get().getAgrirouterEndpointId(), messageId);
                    log.debug("Deleted {} content message, no chunks were harmed.", i);
                }
            } else {
                throw new BusinessException(ErrorMessageFactory.couldNotFindContentMessage());
            }
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }

    private void deleteChunkedMessageContent(String agrirouterEndpointId, String chunkContextId) {
        var nrOfContentMessages = contentMessageRepository.deleteByAgrirouterEndpointIdAndContentMessageMetadataChunkContextId(agrirouterEndpointId, chunkContextId);
        log.debug("Deleted {} content message chunks.", nrOfContentMessages);
    }
}
