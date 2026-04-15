package de.agrirouter.middleware.business.dto;

import de.agrirouter.middleware.domain.enums.TemporaryContentMessageType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageStatisticsTest {

    @Test
    void addMessageStatisticEntry_withNewSender_createsNewEntry() {
        var stats = new MessageStatistics();
        stats.setExternalEndpointId("endpoint-123");

        stats.addMessageStatisticEntry("sender-1",
                new MessageStatistics.MessageStatistic.Entry(TemporaryContentMessageType.ISO_11783_TIME_LOG, 5L));

        assertThat(stats.getMessageStatistics()).hasSize(1);
        assertThat(stats.getMessageStatistics().get("sender-1").getNumberOfMessages()).isEqualTo(5L);
        assertThat(stats.getMessageStatistics().get("sender-1").getEntries()).hasSize(1);
    }

    @Test
    void addMessageStatisticEntry_withExistingSender_accumulatesEntries() {
        var stats = new MessageStatistics();
        stats.addMessageStatisticEntry("sender-1",
                new MessageStatistics.MessageStatistic.Entry(TemporaryContentMessageType.ISO_11783_TIME_LOG, 5L));
        stats.addMessageStatisticEntry("sender-1",
                new MessageStatistics.MessageStatistic.Entry(TemporaryContentMessageType.ISO_11783_DEVICE_DESCRIPTION, 3L));

        assertThat(stats.getMessageStatistics()).hasSize(1);
        var statistic = stats.getMessageStatistics().get("sender-1");
        assertThat(statistic.getNumberOfMessages()).isEqualTo(8L);
        assertThat(statistic.getEntries()).hasSize(2);
    }

    @Test
    void addMessageStatisticEntry_withMultipleSenders_keepsThemSeparate() {
        var stats = new MessageStatistics();
        stats.addMessageStatisticEntry("sender-A",
                new MessageStatistics.MessageStatistic.Entry(TemporaryContentMessageType.ISO_11783_TIME_LOG, 10L));
        stats.addMessageStatisticEntry("sender-B",
                new MessageStatistics.MessageStatistic.Entry(TemporaryContentMessageType.DOC_PDF, 2L));

        assertThat(stats.getMessageStatistics()).hasSize(2);
        assertThat(stats.getMessageStatistics().get("sender-A").getNumberOfMessages()).isEqualTo(10L);
        assertThat(stats.getMessageStatistics().get("sender-B").getNumberOfMessages()).isEqualTo(2L);
    }

    @Test
    void addMessageStatisticEntry_withMultipleEntriesForSameSender_aggregatesCorrectly() {
        var stats = new MessageStatistics();
        stats.addMessageStatisticEntry("sender-X",
                new MessageStatistics.MessageStatistic.Entry(TemporaryContentMessageType.ISO_11783_TIME_LOG, 100L));
        stats.addMessageStatisticEntry("sender-X",
                new MessageStatistics.MessageStatistic.Entry(TemporaryContentMessageType.ISO_11783_DEVICE_DESCRIPTION, 50L));
        stats.addMessageStatisticEntry("sender-X",
                new MessageStatistics.MessageStatistic.Entry(TemporaryContentMessageType.ISO_11783_FIELD, 25L));

        var statistic = stats.getMessageStatistics().get("sender-X");
        assertThat(statistic.getNumberOfMessages()).isEqualTo(175L);
        assertThat(statistic.getEntries()).hasSize(3);
    }

    @Test
    void messageStatistic_constructor_setsInitialValuesCorrectly() {
        var entry = new MessageStatistics.MessageStatistic.Entry(TemporaryContentMessageType.ISO_11783_FARM, 7L);
        var statistic = new MessageStatistics.MessageStatistic(entry);

        assertThat(statistic.getNumberOfMessages()).isEqualTo(7L);
        assertThat(statistic.getEntries()).hasSize(1);
        assertThat(statistic.getEntries().get(0).getTechnicalMessageType()).isEqualTo(TemporaryContentMessageType.ISO_11783_FARM);
        assertThat(statistic.getEntries().get(0).getNumberOfMessages()).isEqualTo(7L);
    }

    @Test
    void messageStatistics_initialState_hasEmptyMap() {
        var stats = new MessageStatistics();

        assertThat(stats.getMessageStatistics()).isEmpty();
    }
}
