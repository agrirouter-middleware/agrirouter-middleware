package de.agrirouter.middleware.business.cache.query;

import com.google.protobuf.Timestamp;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache for the latest query results.
 */
@Component
public class LatestHeaderQueryResults {

    @Getter
    private final Map<String, QueryResult> latestQueryResults;

    public LatestHeaderQueryResults() {
        latestQueryResults = new HashMap<>();
    }

    /**
     * Add a new query result to the cache.
     *
     * @param externalEndpointId External endpoint ID.
     * @param queryResult        Query result.
     */
    public void add(String externalEndpointId, QueryResult queryResult) {
        latestQueryResults.put(externalEndpointId, queryResult);
    }

    public QueryResult get(String externalEndpointId) {
        return latestQueryResults.get(externalEndpointId);
    }


    /**
     * Internal class for storing a single query result.
     */
    @Setter
    @Getter
    public static class QueryResult {
        private int totalMessagesInQuery;
        private int pageNumber;
        private int pageTotal;
        private Instant timestamp;
        private List<MessageDetails> messageDetails;

        public void addMessageDetails(MessageDetails messageDetails) {
            if (this.messageDetails == null) {
                this.messageDetails = new ArrayList<>();
            }
            this.messageDetails.add(messageDetails);
        }

        @Getter
        @Setter
        public static class MessageDetails {
            private String messageId;
            private String technicalMessageType;
            private String fileName;
            private String senderId;
            private Timestamp sentTimestamp;
            private long payloadSize;
        }
    }
}
