package de.agrirouter.middleware.controller.aop.agrirouter.status;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.integration.status.AgrirouterStatusIntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Cancel the execution of the method if the agrirouter is not operational.
 */
@Slf4j
@Aspect
@Component
public class CancelIfAgrirouterIsNotOperationalAspect {

    private final AgrirouterStatusIntegrationService agrirouterStatusIntegrationService;

    public CancelIfAgrirouterIsNotOperationalAspect(AgrirouterStatusIntegrationService agrirouterStatusIntegrationService) {
        this.agrirouterStatusIntegrationService = agrirouterStatusIntegrationService;
    }

    /**
     * Cancel the execution of the method if the agrirouter is not operational.
     */
    @Around(value = "@annotation(CancelIfAgrirouterIsNotOperational)")
    public Object cancelIfAgrirouterIsNotOperational(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (this.agrirouterStatusIntegrationService.isOperational()) {
            return proceedingJoinPoint.proceed();
        } else {
            log.debug("Canceling the execution of the method because the agrirouter is not operational.");
            throw new BusinessException(ErrorMessageFactory.agrirouterStatusNotOperational());
        }
    }

}
