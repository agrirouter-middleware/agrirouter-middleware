package de.agrirouter.middleware.controller.dto.response;

import com.google.protobuf.Timestamp;
import de.agrirouter.middleware.business.cache.query.LatestQueryResults;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Response for the latest query results.
 */
@Schema(description = "Response for the latest query results.")
public class LatestQueryResultsResponse {

    @Getter
    @Schema(description = "The list of the latest query results grouped by application.")
    private final Map<String, QueryResults> latestQueryResults = new HashMap<>();

    /**
     * Add a new query result to the result.
     *
     * @param externalEndpointId External endpoint ID.
     * @param queryResult        Query result.
     */
    public void add(String applicationId, String externalEndpointId, LatestQueryResults.QueryResult queryResult) {
        if (!latestQueryResults.containsKey(applicationId)) {
            latestQueryResults.put(applicationId, new QueryResults());
        }
        latestQueryResults.get(applicationId).add(externalEndpointId, queryResult);
    }

    /**
     * Internal class for storing a multiple query results grouped by endpoint.
     */
    @Schema(description = "Internal class for storing a multiple query results grouped by endpoint.")
    public static class QueryResults {

        @Getter
        @Schema(description = "The list of the latest query results grouped by endpoint.")
        private final Map<String, QueryResult> latestQueryResults = new HashMap<>();

        public void add(String externalEndpointId, LatestQueryResults.QueryResult queryResult) {
            if (null != queryResult) {
                var modelMapper = new ModelMapper();
                latestQueryResults.put(externalEndpointId, modelMapper.map(queryResult, QueryResult.class));
            } else {
                latestQueryResults.put(externalEndpointId, null);
            }
        }

        /**
         * Internal class for storing a single query result.
         */
        @Getter
        @Setter
        @Schema(description = "Internal class for storing a single query result.")
        public static class QueryResult {
            @Schema(description = "Total number of messages in the query.")
            private int totalMessagesInQuery;
            @Schema(description = "Total number of messages in this page.")
            private int messagesCount;
            @Schema(description = "The number of the page.")
            private int pageNumber;
            @Schema(description = "The number of the pages.")
            private int pageTotal;
            @Schema(description = "The timestamp.")
            private Instant timestamp;
            @Schema(description = "The message details.")
            private MessageDetails messageDetails;

            /**
             * The details of a single message.
             */
            @Getter
            @Setter
            public static class MessageDetails {
                @Schema(description = "The message ID.")
                private String messageId;
                @Schema(description = "The technical message type.")
                private String technicalMessageType;
                @Schema(description = "The file name.")
                private String fileName;
                @Schema(description = "The sender ID.")
                private String senderId;
                @Schema(description = "The timestamp the message was sent.")
                private Timestamp sentTimestamp;
                @Schema(description = "The size of the payload.")
                private long payloadSize;
            }
        }
    }
}
