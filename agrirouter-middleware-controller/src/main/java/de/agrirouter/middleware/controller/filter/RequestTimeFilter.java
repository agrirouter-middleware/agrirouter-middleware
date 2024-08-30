package de.agrirouter.middleware.controller.filter;

import de.agrirouter.middleware.controller.SecuredApiController;
import de.agrirouter.middleware.controller.UnsecuredApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.time.Instant;

/**
 * A filter that measures the time taken for processing a servlet request.
 *
 * <p>
 * This filter calculates the time taken for processing a servlet request and logs it using SLF4J
 * logging framework. It measures the time from the start of the request to the end of the
 * request processing, including any filters and servlets.
 *
 * <p>
 * This filter can be used to identify slow requests and detect any performance issues in the
 * application.
 *
 * <p>
 * This filter is implemented as a servlet filter and should be registered with the servlet
 * container for it to take effect. It can be registered by adding the {@code RequestTimeFilter}
 * class as a component and annotating it with {@code @Component}. Additionally, the
 * {@code @WebFilter} annotation should be used to specify the URL patterns that this filter should
 * intercept.
 *
 * <p>
 * When a request is intercepted by this filter, it captures the start time using {@code Instant.now()}
 * before invoking the next filter or servlet in the filter chain. After the request processing
 * is complete, the end time is captured, and the total time taken is calculated by subtracting
 * the start time from the end time. The total time is then logged to the SLF4J logger using the
 * {@code log.info()} method.
 *
 * <p>
 * This filter should be used in conjunction with an appropriate logging configuration.
 *
 * <p>
 * Example usage:
 * <pre>{@code
 *     @Slf4j
 *     @Component
 *     @WebFilter(urlPatterns = {"/api/*"})
 *     public class RequestTimeFilter implements Filter {
 *         // ...
 *     }
 * }</pre>
 */
@Slf4j
@Component
@WebFilter(urlPatterns = {SecuredApiController.API_PREFIX, UnsecuredApiController.API_PREFIX})
public class RequestTimeFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        var start = Instant.now();
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            var end = Instant.now();
            log.trace("The request to '{}' took {} ms", servletRequest.getServletContext().getContextPath(), end.toEpochMilli() - start.toEpochMilli());
        }
    }
}
