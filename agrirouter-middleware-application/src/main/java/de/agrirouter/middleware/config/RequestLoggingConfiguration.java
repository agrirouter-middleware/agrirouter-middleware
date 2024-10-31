package de.agrirouter.middleware.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Logging the incoming requests.
 */
@Configuration
public class RequestLoggingConfiguration {

    /**
     * Filter for logging the requests.
     *
     * @return -
     */
    @Bean
    public CommonsRequestLoggingFilter loggingFilter() {
        final var filter
                = new NoHealthLoggingRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludeHeaders(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        return filter;
    }

    /**
     * Custom implementation to avoid health request logging.
     */
    protected class NoHealthLoggingRequestLoggingFilter extends CommonsRequestLoggingFilter {

        @Override
        protected boolean shouldLog(HttpServletRequest request) {
            return !request.getRequestURI().contains("/actuator/health");
        }
    }

}
