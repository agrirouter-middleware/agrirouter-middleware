package de.agrirouter.middleware.config;

import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.business.security.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SpringSecurityConfiguration {

    private static final String WILDCARD = "/**";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((requests) ->
                        requests.requestMatchers(Routes.SecuredRestEndpoints.ALL_REQUESTS + WILDCARD).hasAuthority(Roles.USER.getKey())
                                .requestMatchers(Routes.MonitoringEndpoints.ACTUATOR).hasAuthority(Roles.MONITORING.getKey())
                                .requestMatchers(Routes.MonitoringEndpoints.ALL_REQUESTS + WILDCARD).hasAuthority(Roles.MONITORING.getKey())
                                .requestMatchers(Routes.MonitoringEndpoints.ACTUATOR + WILDCARD).hasAuthority(Roles.MONITORING.getKey())
                                .requestMatchers(Routes.UserInterface.APPLICATIONS).hasAuthority(Roles.USER.getKey())
                                .requestMatchers(Routes.UserInterface.APPLICATIONS + WILDCARD).hasAuthority(Roles.USER.getKey())
                                .requestMatchers(Routes.UserInterface.ENDPOINT_DASHBOARD).hasAuthority(Roles.USER.getKey())
                                .requestMatchers(Routes.UserInterface.ENDPOINT_DASHBOARD + WILDCARD).hasAuthority(Roles.USER.getKey())
                                .requestMatchers(Routes.UserInterface.ENDPOINTS).hasAuthority(Roles.USER.getKey())
                                .requestMatchers(Routes.UserInterface.ENDPOINTS + WILDCARD).hasAuthority(Roles.USER.getKey())
                                .anyRequest().permitAll(
                                )).httpBasic(Customizer.withDefaults())
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

}
