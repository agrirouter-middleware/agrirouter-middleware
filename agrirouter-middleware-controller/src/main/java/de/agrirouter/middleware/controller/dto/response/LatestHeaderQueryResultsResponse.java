package de.agrirouter.middleware.controller.dto.response;

import de.agrirouter.middleware.business.cache.query.LatestHeaderQueryResults;
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
@Getter
@Schema(description = "Response for the latest header query results.")
public class LatestHeaderQueryResultsResponse {

    @Schema(description = "The list of the latest header query results grouped by application.")
    private final Map<String, QueryResults> latestQueryResults = new HashMap<>();

    /**
     * Add a new header query result to the result.
     *
     * @param externalEndpointId External endpoint ID.
     * @param queryResult        Query result.
     */
    public void add(String applicationId, String externalEndpointId, LatestHeaderQueryResults.QueryResult queryResult) {
        if (!latestQueryResults.containsKey(applicationId)) {
            latestQueryResults.put(applicationId, new QueryResults());
        }
        latestQueryResults.get(applicationId).add(externalEndpointId, queryResult);
    }

    /**
     * Internal class for storing a multiple header query results grouped by endpoint.
     */
    @Getter
    @Schema(description = "Internal class for storing a multiple header query results grouped by endpoint.")
    public static class QueryResults {

        @Schema(description = "The list of the latest header query results grouped by endpoint.")
        private final Map<String, QueryResult> latestQueryResults = new HashMap<>();

        public void add(String externalEndpointId, LatestHeaderQueryResults.QueryResult queryResult) {
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
            @Schema(description = "The number of the page.")
            private int pageNumber;
            @Schema(description = "The number of the pages.")
            private int pageTotal;
            @Schema(description = "The timestamp.")
            private Instant timestamp;
            @Schema(description = "The message details.")
            private LatestQueryResultsResponse.QueryResults.QueryResult.MessageDetails messageDetails;
        }
    }
}
