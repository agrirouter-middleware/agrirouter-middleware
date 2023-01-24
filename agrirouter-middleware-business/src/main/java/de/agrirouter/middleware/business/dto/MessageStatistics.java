package de.agrirouter.middleware.business.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for the message statistics.
 */
@Getter
@Setter
public class MessageStatistics {

    private String externalEndpointId;

    private Map<String, MessageStatistic> messageStatistics = new HashMap<>();

    /**
     * Add a new message statistic to the list.
     *
     * @param senderId The sender ID.
     * @param entry    The new entry for the message statistic.
     */
    public void addMessageStatisticEntry(String senderId, MessageStatistic.Entry entry) {
        if (messageStatistics.containsKey(senderId)) {
            messageStatistics.get(senderId).addEntry(entry);
        } else {
            messageStatistics.put(senderId, new MessageStatistic(entry));
        }
    }

    /**
     * DTO for the message statistic.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public static class MessageStatistic {
        /**
         * Technical message type.
         */
        private long numberOfMessages;

        /**
         * The messages statistics grouped by technical message type.
         */
        private List<Entry> entries;

        public MessageStatistic(Entry entry) {
            this.numberOfMessages = entry.getNumberOfMessages();
            this.entries = new ArrayList<>();
            this.entries.add(entry);
        }

        /**
         * Add a new message statistic entry to the list.
         *
         * @param entry The message statistic entry.
         */
        public void addEntry(Entry entry) {
            if (entries == null) {
                numberOfMessages = entry.getNumberOfMessages();
                entries = new ArrayList<>();
                entries.add(entry);
            } else {
                numberOfMessages += entry.getNumberOfMessages();
                entries.add(entry);
            }
        }

        /**
         * DTO for the message statistic entry.
         */
        @Getter
        @Setter
        @AllArgsConstructor
        public static class Entry {

            /**
             * Technical message type.
             */
            private String technicalMessageType;

            /**
             * Number of messages.
             */
            private long numberOfMessages;
        }
    }
}
