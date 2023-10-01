package de.agrirouter.middleware.config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Provide global beans.
 */
public class GlobalBeanConfiguration {

    public static final Gson GSON = new Gson();

    /**
     * Create a new instance of the Gson library.
     *
     * @return -
     */
    @Bean
    public Gson gson() {
        return GSON;
    }

    /**
     * Define a task executor for the asynchronous tasks.
     */
    @Bean
    public ThreadPoolExecutor taskExecutor() {
        return new ThreadPoolExecutor(10, 10, 0L, java.util.concurrent.TimeUnit.MILLISECONDS, new java.util.concurrent.LinkedBlockingQueue<>());
    }

}
