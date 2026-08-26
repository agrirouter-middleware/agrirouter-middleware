package de.agrirouter.middleware.integration.mqtt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MessageProcessingPoolTest {

    private static final int TIMEOUT_IN_SECONDS = 10;

    private MessageProcessingPool messageProcessingPool;

    @AfterEach
    void tearDown() {
        if (null != messageProcessingPool) {
            messageProcessingPool.shutdown();
        }
    }

    @Test
    void execute_severalTasks_runsThemInParallel() throws InterruptedException {
        messageProcessingPool = new MessageProcessingPool(4, 10);
        final var allTasksStarted = new CountDownLatch(4);
        final var releaseTasks = new CountDownLatch(1);
        final var threadNames = ConcurrentHashMap.<String>newKeySet();

        for (var i = 0; i < 4; i++) {
            messageProcessingPool.execute(() -> {
                threadNames.add(Thread.currentThread().getName());
                allTasksStarted.countDown();
                awaitQuietly(releaseTasks);
            });
        }

        // All four tasks have to be in flight at the same time - this is the whole point of the pool.
        assertThat(allTasksStarted.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)).isTrue();
        releaseTasks.countDown();
        assertThat(threadNames).hasSize(4);
    }

    @Test
    void execute_tasksRunOnDedicatedThreads_notOnTheCallingThread() throws InterruptedException {
        messageProcessingPool = new MessageProcessingPool(2, 10);
        final var taskFinished = new CountDownLatch(1);
        final var threadName = new AtomicReference<String>();

        messageProcessingPool.execute(() -> {
            threadName.set(Thread.currentThread().getName());
            taskFinished.countDown();
        });

        assertThat(taskFinished.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)).isTrue();
        assertThat(threadName.get())
                .isNotEqualTo(Thread.currentThread().getName())
                .startsWith(MessageProcessingPool.THREAD_NAME_PREFIX);
    }

    @Test
    void execute_whenSaturated_appliesBackpressureInsteadOfDroppingTasks() {
        // A single worker and room for a single queued task, so the third task can neither run nor be queued.
        messageProcessingPool = new MessageProcessingPool(1, 1);
        final var blockWorker = new CountDownLatch(1);
        final var executedTasks = new AtomicInteger();

        messageProcessingPool.execute(() -> {
            executedTasks.incrementAndGet();
            awaitQuietly(blockWorker);
        });
        messageProcessingPool.execute(executedTasks::incrementAndGet);

        // The pool is saturated now. This task must not be rejected - it has to run on the calling thread instead,
        // which slows the caller down and therefore throttles the incoming messages.
        final var callingThread = Thread.currentThread().getName();
        final var threadOfThirdTask = new AtomicReference<String>();
        messageProcessingPool.execute(() -> {
            threadOfThirdTask.set(Thread.currentThread().getName());
            executedTasks.incrementAndGet();
        });

        assertThat(threadOfThirdTask.get()).isEqualTo(callingThread);
        blockWorker.countDown();
        messageProcessingPool.shutdown();
        assertThat(executedTasks.get()).isEqualTo(3);
    }

    @Test
    void shutdown_waitsForTheTasksThatAreStillInFlight() throws InterruptedException {
        messageProcessingPool = new MessageProcessingPool(2, 10);
        final var taskStarted = new CountDownLatch(1);
        final var finishedTasks = new AtomicInteger();

        messageProcessingPool.execute(() -> {
            taskStarted.countDown();
            sleepQuietly(200);
            finishedTasks.incrementAndGet();
        });
        assertThat(taskStarted.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)).isTrue();

        messageProcessingPool.shutdown();

        assertThat(finishedTasks.get()).isOne();
    }

    @Test
    void getQueueSize_reflectsTheTasksThatAreWaiting() throws InterruptedException {
        messageProcessingPool = new MessageProcessingPool(1, 10);
        final var workerStarted = new CountDownLatch(1);
        final var blockWorker = new CountDownLatch(1);

        messageProcessingPool.execute(() -> {
            workerStarted.countDown();
            awaitQuietly(blockWorker);
        });
        assertThat(workerStarted.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)).isTrue();
        messageProcessingPool.execute(() -> {
        });

        assertThat(messageProcessingPool.getQueueSize()).isOne();
        assertThat(messageProcessingPool.getActiveCount()).isOne();
        blockWorker.countDown();
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            if (!latch.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Latch was not released in time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
