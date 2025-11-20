package de.agrirouter.middleware.controller.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CorrelationIdFilter}.
 */
@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private FilterChain mockFilterChain;

    private CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void givenRequestWithCorrelationIdHeader_whenDoFilter_thenShouldUseProvidedCorrelationId() throws ServletException, IOException {
        // Given
        String expectedCorrelationId = "test-correlation-id-12345";
        when(mockRequest.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(expectedCorrelationId);

        // When
        filter.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Then
        verify(mockResponse).setHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, expectedCorrelationId);
        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
        
        // MDC should be cleared after processing
        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    }

    @Test
    void givenRequestWithoutCorrelationIdHeader_whenDoFilter_thenShouldGenerateNewCorrelationId() throws ServletException, IOException {
        // Given
        when(mockRequest.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(null);

        // When
        filter.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Then
        verify(mockResponse).setHeader(eq(CorrelationIdFilter.CORRELATION_ID_HEADER), anyString());
        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
        
        // MDC should be cleared after processing
        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    }

    @Test
    void givenRequestWithEmptyCorrelationIdHeader_whenDoFilter_thenShouldGenerateNewCorrelationId() throws ServletException, IOException {
        // Given
        when(mockRequest.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn("   ");

        // When
        filter.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Then
        verify(mockResponse).setHeader(eq(CorrelationIdFilter.CORRELATION_ID_HEADER), anyString());
        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
        
        // MDC should be cleared after processing
        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    }

    @Test
    void givenRequestWithCorrelationIdHeader_whenDoFilter_thenShouldSetMdcDuringProcessing() throws ServletException, IOException {
        // Given
        String expectedCorrelationId = "test-correlation-id-12345";
        when(mockRequest.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(expectedCorrelationId);

        // Use a custom filter chain to verify MDC is set during processing
        FilterChain verifyingFilterChain = (request, response) -> {
            String mdcCorrelationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
            assertEquals(expectedCorrelationId, mdcCorrelationId, "MDC should contain correlation ID during request processing");
        };

        // When
        filter.doFilter(mockRequest, mockResponse, verifyingFilterChain);

        // Then - MDC should be cleared after processing
        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    }

    @Test
    void givenFilterChainThrowsException_whenDoFilter_thenShouldStillCleanUpMdc() throws ServletException, IOException {
        // Given
        String correlationId = "test-correlation-id-12345";
        when(mockRequest.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(correlationId);
        doThrow(new ServletException("Test exception")).when(mockFilterChain).doFilter(mockRequest, mockResponse);

        // When/Then
        assertThrows(ServletException.class, () -> filter.doFilter(mockRequest, mockResponse, mockFilterChain));

        // MDC should still be cleared even if exception occurred
        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    }
}
