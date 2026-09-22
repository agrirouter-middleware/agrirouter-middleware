package de.agrirouter.middleware.integration.mqtt;

import io.micrometer.core.instrument.Metrics;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The threading model for all messages that are incoming from the agrirouter©.
 * <p>
 * A MQTT client delivers the messages of a single connection one after another, therefore the throughput of the
 * middleware would be limited to a single message at a time if the messages were handled while they are delivered.
 * The messages are handed over to this pool instead, so that they are handled in parallel.
 */
@Slf4j
@Component
public class MessageProcessingPool implements Executor {

    static final String THREAD_NAME_PREFIX = "mqtt-message-processing-";
    static final String DELIVERY_THREAD_NAME = "mqtt-message-delivery";

    private static final String QUEUE_SIZE = "middleware.message_processing.queue_size";
    private static final String ACTIVE_WORKERS = "middleware.message_processing.active_workers";
    private static final long KEEP_ALIVE_TIME_IN_SECONDS = 60;
    private static final long SHUTDOWN_TIMEOUT_IN_SECONDS = 30;

    private final ThreadPoolExecutor workers;
    private final ExecutorService deliveryExecutor;

    public MessageProcessingPool(@Value("${app.agrirouter.mqtt.message-processing.pool-size:32}") int poolSize,
                                 @Value("${app.agrirouter.mqtt.message-processing.queue-capacity:500}") int queueCapacity) {
        log.info("Creating the pool for the message processing, using {} threads and a queue capacity of {}.", poolSize, queueCapacity);
        workers = new ThreadPoolExecutor(poolSize,
                poolSize,
                KEEP_ALIVE_TIME_IN_SECONDS,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new PrefixedThreadFactory(THREAD_NAME_PREFIX),
                // Handling the message on the caller slows the delivery of the following messages down. This is the
                // backpressure we want, dropping the message would mean it is neither handled nor confirmed.
                new ThreadPoolExecutor.CallerRunsPolicy());
        workers.allowCoreThreadTimeOut(true);
        deliveryExecutor = Executors.newSingleThreadExecutor(runnable -> {
            var thread = new Thread(runnable, DELIVERY_THREAD_NAME);
            thread.setDaemon(false);
            return thread;
        });
        Metrics.gauge(QUEUE_SIZE, workers, threadPoolExecutor -> threadPoolExecutor.getQueue().size());
        Metrics.gauge(ACTIVE_WORKERS, workers, ThreadPoolExecutor::getActiveCount);
    }

    /**
     * Hand a message over to be handled by one of the workers.
     *
     * @param task The handling of a single message.
     */
    @Override
    public void execute(Runnable task) {
        workers.execute(task);
    }

    /**
     * The executor the MQTT clients deliver their messages on. Delivering on a dedicated thread keeps the network
     * threads of the MQTT client free, which is what keeps the connections alive while the middleware is under load.
     *
     * @return -
     */
    public Executor deliveryExecutor() {
        return deliveryExecutor;
    }

    /**
     * The number of messages that are waiting to be handled.
     *
     * @return -
     */
    public int getQueueSize() {
        return workers.getQueue().size();
    }

    /**
     * The number of messages that are currently being handled.
     *
     * @return -
     */
    public int getActiveCount() {
        return workers.getActiveCount();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down the pool for the message processing, {} messages are still waiting to be handled.", getQueueSize());
        deliveryExecutor.shutdown();
        workers.shutdown();
        if (!awaitTermination(workers)) {
            log.warn("The messages that were still waiting have not been handled within {} seconds, dropping them. They are fetched again during the next scheduled run.", SHUTDOWN_TIMEOUT_IN_SECONDS);
            workers.shutdownNow();
        }
        awaitTermination(deliveryExecutor);
    }

    private boolean awaitTermination(ExecutorService executorService) {
        try {
            return executorService.awaitTermination(SHUTDOWN_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
            return false;
        }
    }

    private static class PrefixedThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger();
        private final String prefix;

        private PrefixedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            var thread = new Thread(runnable, prefix + threadNumber.incrementAndGet());
            thread.setDaemon(false);
            return thread;
        }

    }

}
