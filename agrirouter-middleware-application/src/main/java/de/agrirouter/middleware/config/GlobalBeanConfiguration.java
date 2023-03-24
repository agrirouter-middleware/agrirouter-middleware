package de.agrirouter.middleware.config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;

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

}
