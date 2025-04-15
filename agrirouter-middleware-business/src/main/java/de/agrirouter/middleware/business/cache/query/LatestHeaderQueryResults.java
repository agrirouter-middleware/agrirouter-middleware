package de.agrirouter.middleware.business.cache.query;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache for the latest query results.
 */
@Getter
@Component
public class LatestHeaderQueryResults {

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
    }
}
