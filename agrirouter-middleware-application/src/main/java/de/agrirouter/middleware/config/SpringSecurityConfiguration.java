package de.agrirouter.middleware.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;

import javax.ws.rs.HttpMethod;
import java.util.Arrays;

/**
 * Security configuration.
 */
@Configuration
@Profile({"dev", "qa"})
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SpringSecurityConfiguration(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .cors().configurationSource(request -> {
                    final var configuration = new CorsConfiguration().applyPermitDefaultValues();
                    configuration.setAllowedMethods(Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE));
                    return configuration;
                }).and()
                .authorizeRequests()
                .antMatchers("/private/api", "/private/api/**").authenticated()
                .anyRequest().permitAll()
                .and().httpBasic();
    }

}
