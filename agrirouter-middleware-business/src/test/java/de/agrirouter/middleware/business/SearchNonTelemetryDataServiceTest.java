package de.agrirouter.middleware.business;

import de.agrirouter.middleware.domain.ContentMessageMetadata;
import de.agrirouter.middleware.persistence.jpa.ContentMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SearchNonTelemetryDataServiceTest {

    @Mock
    private ContentMessageRepository contentMessageRepository;

    @Mock
    private EndpointService endpointService;

    @InjectMocks
    private SearchNonTelemetryDataService searchNonTelemetryDataService;

    @Test
    void flattenContentMessageMetadata_withSingleMessages_returnsAllMessages() {
        var msg1 = createSingleMessage("msg-1");
        var msg2 = createSingleMessage("msg-2");
        var msg3 = createSingleMessage("msg-3");

        List<ContentMessageMetadata> result = invokeFlattener(List.of(msg1, msg2, msg3));

        assertThat(result).hasSize(3);
    }

    @Test
    void flattenContentMessageMetadata_withEmptyList_returnsEmptyList() {
        List<ContentMessageMetadata> result = invokeFlattener(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void flattenContentMessageMetadata_withDuplicateMessageIds_deduplicates() {
        var msg1 = createSingleMessage("msg-1");
        var msg1Duplicate = createSingleMessage("msg-1");

        List<ContentMessageMetadata> result = invokeFlattener(List.of(msg1, msg1Duplicate));

        assertThat(result).hasSize(1);
    }

    @Test
    void flattenContentMessageMetadata_withChunkedMessages_returnsOnePerChunkContext() {
        var chunk1 = createChunkedMessage("msg-chunk-1-part1", "chunk-ctx-1");
        var chunk2 = createChunkedMessage("msg-chunk-1-part2", "chunk-ctx-1");
        var chunk3 = createChunkedMessage("msg-chunk-1-part3", "chunk-ctx-1");

        List<ContentMessageMetadata> result = invokeFlattener(List.of(chunk1, chunk2, chunk3));

        assertThat(result).hasSize(1);
    }

    @Test
    void flattenContentMessageMetadata_withMultipleChunkContexts_returnsOnePerContext() {
        var ctx1chunk1 = createChunkedMessage("msg-ctx1-p1", "chunk-ctx-A");
        var ctx1chunk2 = createChunkedMessage("msg-ctx1-p2", "chunk-ctx-A");
        var ctx2chunk1 = createChunkedMessage("msg-ctx2-p1", "chunk-ctx-B");
        var ctx2chunk2 = createChunkedMessage("msg-ctx2-p2", "chunk-ctx-B");

        List<ContentMessageMetadata> result = invokeFlattener(List.of(ctx1chunk1, ctx1chunk2, ctx2chunk1, ctx2chunk2));

        assertThat(result).hasSize(2);
    }

    @Test
    void flattenContentMessageMetadata_withMixedSingleAndChunked_returnsExpectedCount() {
        var single1 = createSingleMessage("msg-single-1");
        var single2 = createSingleMessage("msg-single-2");
        var chunk1 = createChunkedMessage("msg-chunk-1", "chunk-ctx-X");
        var chunk2 = createChunkedMessage("msg-chunk-2", "chunk-ctx-X");

        // 2 singles + 1 chunk context = 3 total
        List<ContentMessageMetadata> result = invokeFlattener(List.of(single1, single2, chunk1, chunk2));

        assertThat(result).hasSize(3);
    }

    @SuppressWarnings("unchecked")
    private List<ContentMessageMetadata> invokeFlattener(List<ContentMessageMetadata> input) {
        return (List<ContentMessageMetadata>) ReflectionTestUtils.invokeMethod(
                searchNonTelemetryDataService, "flattenContentMessageMetadata", input);
    }

    private ContentMessageMetadata createSingleMessage(String messageId) {
        var metadata = new ContentMessageMetadata();
        metadata.setMessageId(messageId);
        // No chunkContextId → single message
        return metadata;
    }

    private ContentMessageMetadata createChunkedMessage(String messageId, String chunkContextId) {
        var metadata = new ContentMessageMetadata();
        metadata.setMessageId(messageId);
        metadata.setChunkContextId(chunkContextId);
        return metadata;
    }
}
