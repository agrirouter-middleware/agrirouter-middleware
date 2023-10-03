package de.agrirouter.middleware.config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Provide global beans.
 */
@Configuration
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
     * Create a new instance of the thread pool executor.
     *
     * @return -
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

}
