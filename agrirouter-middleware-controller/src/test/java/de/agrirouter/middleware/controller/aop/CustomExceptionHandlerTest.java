package de.agrirouter.middleware.controller.aop;

import com.dke.data.agrirouter.api.exception.InvalidHttpStatusException;
import de.agrirouter.middleware.controller.dto.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionHandlerTest {

    private CustomExceptionHandler customExceptionHandler;

    @BeforeEach
    void setUp() {
        customExceptionHandler = new CustomExceptionHandler();
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
    void givenInvalidHttpStatusExceptionWithDifferentStatusCodeWhenHandleThenShouldReturnInternalServerError() {
        // Given
        InvalidHttpStatusException exception = new InvalidHttpStatusException(500);

        // When
        ResponseEntity<ErrorResponse> response = customExceptionHandler.handle(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().errorMessage().contains("agrirouter"));
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
