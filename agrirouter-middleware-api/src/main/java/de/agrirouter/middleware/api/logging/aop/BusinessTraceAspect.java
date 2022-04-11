package de.agrirouter.middleware.api.logging.aop;

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

    /**
     * This method is used to trace the business logic for each public method.
     */
    @Around("execution(public * de.agrirouter.middleware..*.*(..))")
    public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
        final var uuid = UUID.randomUUID().toString();
        final var start = System.currentTimeMillis();
        log.trace("[{}] Method '{}'.", uuid, joinPoint.getSignature().getName());
        if (null != joinPoint.getArgs() && joinPoint.getArgs().length > 0) {
            log.trace("[{}] Arguments: {}", uuid, joinPoint.getArgs());
        }
        final var result = joinPoint.proceed();
        final var executionTime = System.currentTimeMillis() - start;
        log.trace("[{}] Method '{}' took {} ms to complete.", uuid, joinPoint.getSignature().getName(), executionTime);
        return result;
    }

}
