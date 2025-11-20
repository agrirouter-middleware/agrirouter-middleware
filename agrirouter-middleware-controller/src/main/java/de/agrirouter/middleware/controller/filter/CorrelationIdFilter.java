package de.agrirouter.middleware.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * A filter that extracts or generates a correlation ID for request tracking across distributed systems.
 *
 * <p>
 * This filter handles the X-Correlation-ID header to enable request tracing across microservices.
 * It performs the following operations:
 * <ul>
 *     <li>Extracts the X-Correlation-ID from incoming request headers if present</li>
 *     <li>Generates a new UUID-based correlation ID if none is provided</li>
 *     <li>Adds the correlation ID to the MDC (Mapped Diagnostic Context) for logging</li>
 *     <li>Includes the correlation ID in response headers for async responses</li>
 *     <li>Logs the correlation ID for request tracking</li>
 * </ul>
 *
 * <p>
 * The correlation ID is placed in the MDC with the key "correlationId", making it available
 * to all log statements within the request processing thread. The MDC is automatically cleared
 * after request processing completes to prevent thread pool pollution.
 *
 * <p>
 * This filter is ordered to execute early in the filter chain (HIGHEST_PRECEDENCE + 1) to ensure
 * the correlation ID is available for all subsequent filters and request processing.
 *
 * <p>
 * Example log pattern to include correlation ID:
 * <pre>
 * %d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} [%X{correlationId}] - %msg%n
 * </pre>
 */
@Slf4j
@Component
@WebFilter(urlPatterns = {"/*"})
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class CorrelationIdFilter implements Filter {

    /**
     * The HTTP header name for the correlation ID.
     */
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    /**
     * The MDC key for the correlation ID.
     */
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        if (servletRequest instanceof HttpServletRequest httpRequest &&
                servletResponse instanceof HttpServletResponse httpResponse) {

            String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = UUID.randomUUID().toString();
                log.debug("Generated new correlation ID: {}", correlationId);
            } else {
                log.debug("Using provided correlation ID: {}", correlationId);
            }
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
            try {
                filterChain.doFilter(servletRequest, servletResponse);
            } finally {
                MDC.remove(CORRELATION_ID_MDC_KEY);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}
