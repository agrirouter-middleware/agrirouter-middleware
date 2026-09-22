package de.agrirouter.middleware.integration.mqtt;

import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import de.agrirouter.middleware.integration.mqtt.health.HealthStatusMessages;
import de.agrirouter.middleware.integration.mqtt.list_endpoints.ListEndpointsMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MessageHandlingCallbackTest {

    private static final String PAYLOAD = """
            {
              "sensorAlternateId": "0ea27b0e-8f34-4b32-9e2a-1b0a6e1a1f3d",
              "capabilityAlternateId": "1c4c8a37-0b1d-4b6f-9d1f-0d7a1a5a0e1b",
              "command": {
                "message": "dGhpcy1pcy1ub3QtcmVhbGx5LWEtbWVzc2FnZQ==",
                "timestamp": "2026-08-26T10:15:30Z"
              }
            }
            """;

    private ApplicationEventPublisher applicationEventPublisher;
    private DecodeMessageService decodeMessageService;
    private MessageProcessingPool messageProcessingPool;
    private MessageHandlingCallback messageHandlingCallback;

    @BeforeEach
    void setUp() {
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        decodeMessageService = mock(DecodeMessageService.class);
        messageProcessingPool = mock(MessageProcessingPool.class);
        messageHandlingCallback = new MessageHandlingCallback(applicationEventPublisher,
                decodeMessageService,
                mock(MqttStatistics.class),
                mock(ListEndpointsMessages.class),
                mock(HealthStatusMessages.class),
                messageProcessingPool);
    }

    @Test
    void accept_doesNotDecodeOrPublishOnTheCallingThread() {
        messageHandlingCallback.accept(publishWith(PAYLOAD));

        // Decoding and persisting a message costs far more than reading it from the wire. None of it may happen on the
        // thread that delivers the message, otherwise the throughput is capped at a single message at a time.
        verifyNoInteractions(decodeMessageService);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void accept_handsTheMessageOverToTheProcessingPool() {
        messageHandlingCallback.accept(publishWith(PAYLOAD));

        verify(messageProcessingPool).execute(any());
    }

    @Test
    void accept_theHandedOverTaskDoesTheActualWork() {
        messageHandlingCallback.accept(publishWith(PAYLOAD));
        final var task = captureHandedOverTask();

        task.run();

        verify(decodeMessageService).decode(anyString());
    }

    @Test
    void accept_readsThePayloadBeforeHandingOver_soTheTaskDoesNotTouchTheMqttMessage() {
        final var mqtt3Publish = publishWith(PAYLOAD);

        messageHandlingCallback.accept(mqtt3Publish);
        final var task = captureHandedOverTask();
        clearInvocations(mqtt3Publish);
        task.run();

        // The payload buffer belongs to the MQTT client and is only guaranteed to be valid while the message is being
        // delivered, so the task has to work on the copy that was taken during delivery.
        verifyNoInteractions(mqtt3Publish);
    }

    @Test
    void accept_whenTheTaskFails_theFailureDoesNotEscapeToTheProcessingThread() {
        when(decodeMessageService.decode(anyString())).thenThrow(new IllegalStateException("Broken message."));
        messageHandlingCallback.accept(publishWith(PAYLOAD));
        final var task = captureHandedOverTask();

        // A single broken message must never take down a worker of the pool.
        task.run();
    }

    @Test
    void accept_countsTheArrivedMessageWhileDelivering() {
        final var mqttStatistics = mock(MqttStatistics.class);
        final var callback = new MessageHandlingCallback(applicationEventPublisher,
                decodeMessageService,
                mqttStatistics,
                mock(ListEndpointsMessages.class),
                mock(HealthStatusMessages.class),
                messageProcessingPool);

        callback.accept(publishWith(PAYLOAD));

        verify(mqttStatistics).increaseNumberOfMessagesArrived();
    }

    private Runnable captureHandedOverTask() {
        final var taskCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(messageProcessingPool).execute(taskCaptor.capture());
        return taskCaptor.getValue();
    }

    private static Mqtt3Publish publishWith(String payload) {
        final var mqtt3Publish = mock(Mqtt3Publish.class);
        when(mqtt3Publish.getPayloadAsBytes()).thenReturn(payload.getBytes(StandardCharsets.UTF_8));
        return mqtt3Publish;
    }

}
