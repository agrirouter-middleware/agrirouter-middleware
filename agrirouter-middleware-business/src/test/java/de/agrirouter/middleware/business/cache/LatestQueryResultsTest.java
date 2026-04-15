package de.agrirouter.middleware.business.cache;

import de.agrirouter.middleware.business.cache.query.LatestQueryResults;
import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LatestQueryResultsTest {

    private final LatestQueryResults latestQueryResults = new LatestQueryResults();

    @Test
    void add_andGet_returnsQueryResult() {
        var externalEndpointId = "endpoint-123";
        var queryResult = new LatestQueryResults.QueryResult();
        queryResult.setTotalMessagesInQuery(10);
        queryResult.setMessagesCount(5);
        queryResult.setPageNumber(1);
        queryResult.setPageTotal(2);
        queryResult.setTimestamp(Instant.now());

        latestQueryResults.add(externalEndpointId, queryResult);

        var result = latestQueryResults.get(externalEndpointId);
        assertThat(result).isNotNull();
        assertThat(result.getTotalMessagesInQuery()).isEqualTo(10);
        assertThat(result.getMessagesCount()).isEqualTo(5);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.getPageTotal()).isEqualTo(2);
    }

    @Test
    void get_withNonExistentEndpoint_returnsNull() {
        var result = latestQueryResults.get("non-existent-endpoint");

        assertThat(result).isNull();
    }

    @Test
    void add_overwritesExistingEntry() {
        var externalEndpointId = "endpoint-overwrite";
        var queryResult1 = new LatestQueryResults.QueryResult();
        queryResult1.setTotalMessagesInQuery(5);
        var queryResult2 = new LatestQueryResults.QueryResult();
        queryResult2.setTotalMessagesInQuery(15);

        latestQueryResults.add(externalEndpointId, queryResult1);
        latestQueryResults.add(externalEndpointId, queryResult2);

        assertThat(latestQueryResults.get(externalEndpointId).getTotalMessagesInQuery()).isEqualTo(15);
    }

    @Test
    void queryResult_addMessageDetails_addsToList() {
        var queryResult = new LatestQueryResults.QueryResult();
        var details = new LatestQueryResults.QueryResult.MessageDetails();
        details.setMessageId("msg-1");
        details.setTechnicalMessageType(TemporaryContentMessageType.ISO_11783_TIME_LOG);
        details.setPayloadSize(1024L);

        queryResult.addMessageDetails(details);

        assertThat(queryResult.getMessageDetails()).hasSize(1);
        assertThat(queryResult.getMessageDetails().get(0).getMessageId()).isEqualTo("msg-1");
        assertThat(queryResult.getMessageDetails().get(0).getTechnicalMessageType())
                .isEqualTo(TemporaryContentMessageType.ISO_11783_TIME_LOG);
    }

    @Test
    void queryResult_addMultipleMessageDetails_appendsAll() {
        var queryResult = new LatestQueryResults.QueryResult();

        for (int i = 0; i < 3; i++) {
            var details = new LatestQueryResults.QueryResult.MessageDetails();
            details.setMessageId("msg-" + i);
            queryResult.addMessageDetails(details);
        }

        assertThat(queryResult.getMessageDetails()).hasSize(3);
    }

    @Test
    void initialState_hasEmptyMap() {
        assertThat(latestQueryResults.getLatestQueryResults()).isEmpty();
    }
}
