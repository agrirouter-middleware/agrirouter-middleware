package de.agrirouter.middleware.api.logging.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BusinessTraceAspectTest {

    private BusinessTraceAspect businessTraceAspect;
    private ObjectMapper objectMapper;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        businessTraceAspect = new BusinessTraceAspect(objectMapper);
    }

    @Test
    void givenSimpleArgumentsWhenTracingThenShouldSerializeToJson() throws Throwable {
        // Given
        Object[] args = new Object[]{"test", 123};
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.getSignature()).thenReturn(mock(org.aspectj.lang.Signature.class));
        when(proceedingJoinPoint.getSignature().getName()).thenReturn("testMethod");
        when(proceedingJoinPoint.proceed()).thenReturn("result");

        // When
        Object result = businessTraceAspect.trace(proceedingJoinPoint);

        // Then
        assertNotNull(result);
        assertEquals("result", result);
        verify(proceedingJoinPoint, times(1)).proceed();
    }

    @Test
    void givenObjectArgumentWhenTracingThenShouldSerializeToJson() throws Throwable {
        // Given
        TestObject testObject = new TestObject("value1", 42);
        Object[] args = new Object[]{testObject};
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.getSignature()).thenReturn(mock(org.aspectj.lang.Signature.class));
        when(proceedingJoinPoint.getSignature().getName()).thenReturn("testMethod");
        when(proceedingJoinPoint.proceed()).thenReturn("result");

        // When
        Object result = businessTraceAspect.trace(proceedingJoinPoint);

        // Then
        assertNotNull(result);
        assertEquals("result", result);
        verify(proceedingJoinPoint, times(1)).proceed();
    }

    @Test
    void givenNoArgumentsWhenTracingThenShouldNotLogArguments() throws Throwable {
        // Given
        when(proceedingJoinPoint.getArgs()).thenReturn(new Object[0]);
        when(proceedingJoinPoint.getSignature()).thenReturn(mock(org.aspectj.lang.Signature.class));
        when(proceedingJoinPoint.getSignature().getName()).thenReturn("testMethod");
        when(proceedingJoinPoint.proceed()).thenReturn("result");

        // When
        Object result = businessTraceAspect.trace(proceedingJoinPoint);

        // Then
        assertNotNull(result);
        assertEquals("result", result);
        verify(proceedingJoinPoint, times(1)).proceed();
    }

    @Test
    void givenNullArgumentsWhenTracingThenShouldNotLogArguments() throws Throwable {
        // Given
        when(proceedingJoinPoint.getArgs()).thenReturn(null);
        when(proceedingJoinPoint.getSignature()).thenReturn(mock(org.aspectj.lang.Signature.class));
        when(proceedingJoinPoint.getSignature().getName()).thenReturn("testMethod");
        when(proceedingJoinPoint.proceed()).thenReturn("result");

        // When
        Object result = businessTraceAspect.trace(proceedingJoinPoint);

        // Then
        assertNotNull(result);
        assertEquals("result", result);
        verify(proceedingJoinPoint, times(1)).proceed();
    }

    // Helper class for testing
    static class TestObject {
        private final String field1;
        private final int field2;

        public TestObject(String field1, int field2) {
            this.field1 = field1;
            this.field2 = field2;
        }

        public String getField1() {
            return field1;
        }

        public int getField2() {
            return field2;
        }
    }
}
