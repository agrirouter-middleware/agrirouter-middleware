package de.agrirouter.middleware.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfiguration {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 25;

    /**
     * Create a new instance of the thread pool task executor.
     *
     * @return -
     */
    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        log.info("Creating thread pool executor, using a core pool size of 5, a max pool size of 10, and a queue capacity of 25.");
        var taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(CORE_POOL_SIZE);
        taskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        taskExecutor.setQueueCapacity(QUEUE_CAPACITY);
        return taskExecutor;
    }

    /**
     * Create a new instance of the thread pool executor.
     *
     * @return -
     */
    @Bean
    public ExecutorService threadPoolExecutor() {
        log.info("Creating thread pool executor, using a core pool size of 5, a max pool size of 10, a queue capacity of 25 and a keep alive time of o.");
        return Executors.newFixedThreadPool(10);
    }

}
