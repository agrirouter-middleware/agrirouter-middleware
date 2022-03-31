package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.business.parameters.SearchNonTelemetryDataParameters;
import de.agrirouter.middleware.domain.ContentMessageMetadata;
import de.agrirouter.middleware.persistence.ContentMessageRepository;
import de.agrirouter.middleware.persistence.EndpointRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;

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
                        searchNonTelemetryDataParameters.getSendFrom(),
                        searchNonTelemetryDataParameters.getSendTo());
                log.debug("Found {} content messages in total.", contentMessageMetadata.size());
            } else {
                contentMessageMetadata = contentMessageRepository.findMetadata(optionalEndpoint.get().getAgrirouterEndpointId(),
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
        contentMessageMetadata.forEach(cmm -> {
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
        final var base64EncodedMessageContent = download(externalEndpointId, messageId);
        return Base64.getDecoder().decode(base64EncodedMessageContent);
    }

    private byte[] download(String externalEndpointId, String messageId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var optionalContentMessage = contentMessageRepository.findByAgrirouterEndpointIdAndContentMessageMetadataMessageId(optionalEndpoint.get().getAgrirouterEndpointId(), messageId);
            if (optionalContentMessage.isPresent()) {
                final var contentMessage = optionalContentMessage.get();
                if (contentMessage.getContentMessageMetadata().getTotalChunks() > 1) {
                    log.debug("Looks like we have multiple chunks for the content message. Assembling the message content first. There are {} chunks in total.", contentMessage.getContentMessageMetadata().getTotalChunks());
                    // FIXME
                    throw new RuntimeException("Not yet implemented.");
                } else {
                    log.debug("This is a single message, therefore returning the content 'as it is'.");
                    return contentMessage.getMessageContent();
                }
            } else {
                throw new BusinessException(ErrorMessageFactory.couldNotFindContentMessage());
            }
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpoint());
        }
    }
}
