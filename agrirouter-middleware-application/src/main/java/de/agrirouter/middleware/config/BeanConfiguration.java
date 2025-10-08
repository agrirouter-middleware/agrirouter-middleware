package de.agrirouter.middleware.config;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.modelmapper.record.RecordModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provide global beans.
 */
@Configuration
public class BeanConfiguration {

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
     * Dependency injection for the model mapper.
     *
     * @return -
     */
    @Bean
    public ModelMapper modelMapper() {
        var modelMapper = new ModelMapper();
        modelMapper.registerModule(new RecordModule());
        return modelMapper;
    }

    /**
     * The password encoder.
     *
     * @return -
     */
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}
