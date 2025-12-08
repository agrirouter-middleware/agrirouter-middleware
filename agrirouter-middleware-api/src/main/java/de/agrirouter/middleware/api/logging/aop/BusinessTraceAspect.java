package de.agrirouter.middleware.api.logging.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * This class is used to trace the business logic.
 */
@Aspect
@Slf4j
@Component
public class BusinessTraceAspect {

    private final ObjectMapper objectMapper;

    public BusinessTraceAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * This method is used to trace the business logic for each public method.
     */
    @Around("execution(public * de.agrirouter.middleware..*.*(..))")
    public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
        final var uuid = UUID.randomUUID().toString();
        final var start = System.currentTimeMillis();
        log.trace("[{}] Method '{}'.", uuid, joinPoint.getSignature().getName());
        if (null != joinPoint.getArgs() && joinPoint.getArgs().length > 0) {
            log.trace("[{}] Arguments: {}", uuid, serializeArguments(joinPoint.getArgs()));
        }
        final var result = joinPoint.proceed();
        final var executionTime = System.currentTimeMillis() - start;
        log.trace("[{}] Method '{}' took {} ms to complete.", uuid, joinPoint.getSignature().getName(), executionTime);
        return result;
    }

    /**
     * Serialize arguments to JSON representation.
     * Falls back to toString() if JSON serialization fails.
     */
    private String serializeArguments(Object[] args) {
        try {
            return objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            log.trace("Failed to serialize arguments to JSON, falling back to toString(): {}", e.getMessage());
            return java.util.Arrays.toString(args);
        }
    }

}
