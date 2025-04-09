package de.agrirouter.middleware.integration.mqtt;

import io.micrometer.core.instrument.Metrics;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Transient statistics for the MQTT connections.
 */
@Slf4j
@ToString
@Component
public class MqttStatistics {

    private static final String NUMBER_OF_CONNECTION_LOSSES = "middleware.number_of_connection_losses";
    private static final String NUMBER_OF_CACHE_MISSES = "middleware.number_of_cache_misses";
    private static final String NUMBER_OF_CLIENT_INITIALIZATIONS = "middleware.number_of_client_initializations";
    private static final String NUMBER_OF_CONNECTS = "middleware.number_of_connects";
    private static final String NUMBER_OF_MESSAGES_ARRIVED = "middleware.number_of_messages_arrived";
    private static final String NUMBER_OF_MESSAGES_PUBLISHED = "middleware.number_of_messages_published";
    private static final String NUMBER_OF_ACKNOWLEDGEMENTS = "middleware.number_of_acknowledgements";
    private static final String NUMBER_OF_PUSH_NOTIFICATIONS = "middleware.number_of_push_notifications";
    private static final String NUMBER_OF_CLOUD_REGISTRATIONS = "middleware.number_of_cloud_registrations";
    private static final String NUMBER_OF_ENDPOINT_LISTINGS = "middleware.number_of_endpoint_listings";
    private static final String NUMBER_OF_UNKNOWN_MESSAGES = "middleware.number_of_unknown_messages";
    private static final String PAYLOAD_RECEIVED = "middleware.payload_received";

    public void increaseNumberOfConnectionLosses() {
        Metrics.counter(NUMBER_OF_CONNECTION_LOSSES).increment();
    }

    public void increaseNumberOfCacheMisses() {
        Metrics.counter(NUMBER_OF_CACHE_MISSES).increment();
    }

    public void increaseNumberOfClientInitializations() {
        Metrics.counter(NUMBER_OF_CLIENT_INITIALIZATIONS).increment();
    }

    public void increaseNumberOfConnects() {
        Metrics.counter(NUMBER_OF_CONNECTS).increment();
    }

    public void increaseNumberOfMessagesArrived() {
        Metrics.counter(NUMBER_OF_MESSAGES_ARRIVED).increment();
    }

    public void increaseNumberOfMessagesPublished() {
        Metrics.counter(NUMBER_OF_MESSAGES_PUBLISHED).increment();
    }

    public void increaseNumberOfAcknowledgements() {
        Metrics.counter(NUMBER_OF_ACKNOWLEDGEMENTS).increment();
    }

    public void increaseNumberOfPushNotifications() {
        Metrics.counter(NUMBER_OF_PUSH_NOTIFICATIONS).increment();
    }

    public void increaseNumberOfCloudRegistrations() {
        Metrics.counter(NUMBER_OF_CLOUD_REGISTRATIONS).increment();
    }

    public void increaseNumberOfEndpointListings() {
        Metrics.counter(NUMBER_OF_ENDPOINT_LISTINGS).increment();
    }

    public void increaseNumberOfUnknownMessages() {
        Metrics.counter(NUMBER_OF_UNKNOWN_MESSAGES).increment();
    }

    public void increaseNumberOfContentMessagesReceived(String technicalMessageType) {
        Metrics.counter(NUMBER_OF_MESSAGES_ARRIVED, "technical_message_type", technicalMessageType).increment();
    }

    public void increasePayloadReceived(int length) {
        Metrics.counter(PAYLOAD_RECEIVED).increment(length);
    }

}
