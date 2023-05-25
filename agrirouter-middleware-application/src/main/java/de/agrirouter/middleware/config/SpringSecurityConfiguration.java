package de.agrirouter.middleware.config;

import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.business.security.Roles;
import org.springframework.context.annotation.Configuration;
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
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String WILDCARD = "/**";

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
                // Secure all REST endpoints.
                .antMatchers(Routes.SecuredRestEndpoints.ALL_REQUESTS + WILDCARD).hasAuthority(Roles.USER.getKey())
                // Secure all monitoring endpoints.
                .antMatchers(Routes.MonitoringEndpoints.ACTUATOR).hasAuthority(Roles.MONITORING.getKey())
                .antMatchers(Routes.MonitoringEndpoints.ALL_REQUESTS + WILDCARD).hasAuthority(Roles.MONITORING.getKey())
                .antMatchers(Routes.MonitoringEndpoints.ACTUATOR + WILDCARD).hasAuthority(Roles.MONITORING.getKey())
                // Secure the user interface.
                .antMatchers(Routes.UserInterface.APPLICATIONS).hasAuthority(Roles.USER.getKey())
                .antMatchers(Routes.UserInterface.APPLICATIONS + WILDCARD).hasAuthority(Roles.USER.getKey())
                .antMatchers(Routes.UserInterface.ENDPOINT_DASHBOARD).hasAuthority(Roles.USER.getKey())
                .antMatchers(Routes.UserInterface.ENDPOINT_DASHBOARD + WILDCARD).hasAuthority(Roles.USER.getKey())
                .antMatchers(Routes.UserInterface.ENDPOINTS).hasAuthority(Roles.USER.getKey())
                .antMatchers(Routes.UserInterface.ENDPOINTS + WILDCARD).hasAuthority(Roles.USER.getKey())
                // Define rules for any request.
                .anyRequest()
                .permitAll()
                .and().httpBasic();
    }

}
