package de.agrirouter.middleware.business.cache;

import de.agrirouter.middleware.business.cache.query.LatestHeaderQueryResults;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LatestHeaderQueryResultsTest {

    private final LatestHeaderQueryResults latestHeaderQueryResults = new LatestHeaderQueryResults();

    @Test
    void add_andGet_returnsQueryResult() {
        var externalEndpointId = "endpoint-header-123";
        var queryResult = new LatestHeaderQueryResults.QueryResult();
        queryResult.setTotalMessagesInQuery(20);
        queryResult.setPageNumber(2);
        queryResult.setPageTotal(4);
        queryResult.setTimestamp(Instant.now());

        latestHeaderQueryResults.add(externalEndpointId, queryResult);

        var result = latestHeaderQueryResults.get(externalEndpointId);
        assertThat(result).isNotNull();
        assertThat(result.getTotalMessagesInQuery()).isEqualTo(20);
        assertThat(result.getPageNumber()).isEqualTo(2);
        assertThat(result.getPageTotal()).isEqualTo(4);
    }

    @Test
    void get_withNonExistentEndpoint_returnsNull() {
        var result = latestHeaderQueryResults.get("non-existent-endpoint");

        assertThat(result).isNull();
    }

    @Test
    void add_overwritesExistingEntry() {
        var externalEndpointId = "endpoint-overwrite";
        var queryResult1 = new LatestHeaderQueryResults.QueryResult();
        queryResult1.setTotalMessagesInQuery(10);
        var queryResult2 = new LatestHeaderQueryResults.QueryResult();
        queryResult2.setTotalMessagesInQuery(30);

        latestHeaderQueryResults.add(externalEndpointId, queryResult1);
        latestHeaderQueryResults.add(externalEndpointId, queryResult2);

        assertThat(latestHeaderQueryResults.get(externalEndpointId).getTotalMessagesInQuery()).isEqualTo(30);
    }

    @Test
    void add_multipleEndpoints_storesThemIndependently() {
        var endpoint1 = "endpoint-header-A";
        var endpoint2 = "endpoint-header-B";
        var qr1 = new LatestHeaderQueryResults.QueryResult();
        qr1.setTotalMessagesInQuery(5);
        var qr2 = new LatestHeaderQueryResults.QueryResult();
        qr2.setTotalMessagesInQuery(15);

        latestHeaderQueryResults.add(endpoint1, qr1);
        latestHeaderQueryResults.add(endpoint2, qr2);

        assertThat(latestHeaderQueryResults.get(endpoint1).getTotalMessagesInQuery()).isEqualTo(5);
        assertThat(latestHeaderQueryResults.get(endpoint2).getTotalMessagesInQuery()).isEqualTo(15);
    }

    @Test
    void initialState_hasEmptyMap() {
        assertThat(latestHeaderQueryResults.getLatestQueryResults()).isEmpty();
    }
}
