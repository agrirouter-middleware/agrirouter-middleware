package de.agrirouter.middleware.controller.aop;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.dke.data.agrirouter.api.exception.InvalidHttpStatusException;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionHandlerTest {

    private CustomExceptionHandler customExceptionHandler;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        customExceptionHandler = new CustomExceptionHandler();
        
        // Set up logging appender to capture log messages
        logger = (Logger) LoggerFactory.getLogger(CustomExceptionHandler.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        // Clean up appender
        logger.detachAppender(listAppender);
    }

    @Test
    void givenInvalidHttpStatusExceptionWhenHandleThenShouldReturnInternalServerError() {
        // Given
        InvalidHttpStatusException exception = new InvalidHttpStatusException(400);

        // When
        ResponseEntity<ErrorResponse> response = customExceptionHandler.handle(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().errorMessage().contains("agrirouter"));
    }

    @Test
    void givenInvalidHttpStatusExceptionWhenHandleThenShouldLogCorrectMessage() {
        // Given
        InvalidHttpStatusException exception = new InvalidHttpStatusException(400);

        // When
        customExceptionHandler.handle(exception);

        // Then
        var logsList = listAppender.list;
        assertEquals(1, logsList.size());
        
        ILoggingEvent logEvent = logsList.get(0);
        assertEquals(Level.ERROR, logEvent.getLevel());
        assertEquals("An invalid HTTP status exception occurred while communicating with the agrirouter.", logEvent.getFormattedMessage());
        assertNotNull(logEvent.getThrowableProxy());
        assertEquals(InvalidHttpStatusException.class.getName(), logEvent.getThrowableProxy().getClassName());
    }

    @Test
    void givenInvalidHttpStatusExceptionWhenHandleThenShouldLogExceptionWithStatusCode() {
        // Given
        InvalidHttpStatusException exception = new InvalidHttpStatusException(500);

        // When
        customExceptionHandler.handle(exception);

        // Then
        var logsList = listAppender.list;
        assertEquals(1, logsList.size());
        
        ILoggingEvent logEvent = logsList.get(0);
        assertEquals(Level.ERROR, logEvent.getLevel());
        assertNotNull(logEvent.getThrowableProxy());
        assertTrue(logEvent.getThrowableProxy().getMessage().contains("500"));
    }

    @Test
    void givenRuntimeExceptionWhenHandleThenShouldReturnInternalServerError() {
        // Given
        RuntimeException exception = new RuntimeException("Generic runtime error");

        // When
        ResponseEntity<ErrorResponse> response = customExceptionHandler.handleUnknownRuntimeException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().errorMessage().contains("unknown error"));
    }
}
