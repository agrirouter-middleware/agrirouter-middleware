package de.agrirouter.middleware.controller.dto;

import de.agrirouter.middleware.business.dto.MessageStatistics;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Response for the message statistics.
 */
@Getter
@Setter
@Schema(description = "Response for the message statistics.")
public class MessageStatisticsGroupedByApplicationResponse {

    private Map<String, List<MessageStatisticGroupedBySender>> messageStatistics = new HashMap<>();

    /**
     * Add a new message statistic for the endpoint to the list.
     *
     * @param applicationId                   The application ID.
     * @param messageStatisticGroupedBySender The message statistics grouped by sender.
     */
    public void add(String applicationId, MessageStatisticGroupedBySender messageStatisticGroupedBySender) {
        if (!messageStatistics.containsKey(applicationId)) {
            var value = new ArrayList<MessageStatisticGroupedBySender>();
            value.add(messageStatisticGroupedBySender);
            messageStatistics.put(applicationId, value);
        } else {
            messageStatistics.get(applicationId).add(messageStatisticGroupedBySender);
        }
    }

    /**
     * The message statistics grouped by sender.
     */
    @Getter
    @Setter
    @Schema(description = "The message statistics grouped by sender.")
    public static class MessageStatisticGroupedBySender {

        @Schema(description = "The external endpoint ID of the recipient.")
        private String externalEndpointId;

        /**
         * The list of the message statistics grouped by sender.
         */
        @Schema(description = "The message statistics grouped by sender.")
        private Map<String, MessageStatistics.MessageStatistic> messageStatistics;

        /**
         * DTO for the message statistic.
         */
        @Getter
        @Setter
        @Schema(description = "Single message statistic for the sender.")
        public static class MessageStatistic {

            /**
             * Technical message type.
             */
            @Schema(description = "The overall number of messages.")
            private long numberOfMessages;

            /**
             * The messages statistics grouped by technical message type.
             */
            @Schema(description = "The messages statistics grouped by technical message type.")
            private List<Entry> entries;

            /**
             * DTO for the message statistic entry.
             */
            @Getter
            @Setter
            @Schema(description = "Single message statistic entry for a technical message type.")
            public static class Entry {

                /**
                 * Technical message type.
                 */
                @Schema(description = "The technical message type.")
                private String technicalMessageType;

                /**
                 * Number of messages.
                 */
                @Schema(description = "The number of messages.")
                private long numberOfMessages;
            }
        }
    }
}
